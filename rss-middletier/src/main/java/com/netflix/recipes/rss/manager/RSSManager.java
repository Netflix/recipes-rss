/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.netflix.recipes.rss.manager;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.NotFoundException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.netflix.client.ClientFactory;
import com.netflix.niws.client.http.HttpClientRequest;
import com.netflix.niws.client.http.HttpClientResponse;
import com.netflix.niws.client.http.RestClient;
import com.netflix.recipes.rss.RSS;
import com.netflix.recipes.rss.RSSConstants;
import com.netflix.recipes.rss.RSSItem;
import com.netflix.recipes.rss.impl.RSSImpl;
import com.netflix.recipes.rss.impl.RSSItemImpl;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RSS Manager that
 *  1) Fetches content from RSS feeds using Ribbon
 *  2) Parses RSS feeds
 *  3) Persists feed urls into Cassandra using Astyanax
 *
 * TODO:
 *  1) Add support to use Cookies as the store and have an option to choose cassandra/cookies
 *  2) Add Servo metrics for Cassandra latencies
 *  3) Move all Cassandra meta info to config
 */
public class RSSManager {

    private static final RSSManager instance = new RSSManager();
    private static Keyspace ks;

    // Data model is documented in the wiki
    private static final ColumnFamily<String, String> CF_SUBSCRIPTIONS = new ColumnFamily<String, String>("Subscriptions", StringSerializer.get(), StringSerializer.get());
    
    private static final Logger logger = LoggerFactory.getLogger(RSSManager.class);
    
    private RSSManager() {
    }

    /**
     * Fetch the RSS feed content using Ribbon
     */
    public RSS fetchRSSFeed(String url) {

        RestClient client = (RestClient) ClientFactory.getNamedClient(RSSConstants.MIDDLETIER_REST_CLIENT);
        HttpClientResponse response = null;
        String rssData = null;

        try {
            HttpClientRequest request = HttpClientRequest.newBuilder().setUri(new URI(url)).build();
            response = client.execute(request);

            if (response != null) {
                rssData  = IOUtils.toString(response.getRawEntity(), Charsets.UTF_8);
                logger.info("Status code for " + response.getRequestedURI() + " : " + response.getStatus());
            }
        } catch (URISyntaxException e) {
            logger.error("Exception occurred when setting the URI", e);
        } catch (Exception e) {
            logger.error("Exception occurred when executing the HTTP request", e);
        }

        return parseRSS(url, rssData);
    }
    
    public static RSSManager getInstance() {
        return instance;
    }

    /**
     * Parses the RSS feeds and return back a POJO
     */
    public RSS parseRSS(String url, String rss) {
        // Error case
        if (rss == null) return new RSSImpl();
        
        RSS rssItems = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            try {
                InputSource is = new InputSource(new StringReader(rss));
                Document dom = db.parse(is);
                Element docEle = dom.getDocumentElement();
                
                List<RSSItem> items = new ArrayList<RSSItem>();
                String title = docEle.getElementsByTagName("title").item(0).getTextContent();
                NodeList nl  = docEle.getElementsByTagName("item");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0 ; i < nl.getLength(); i++) {
                        Element el = (Element) nl.item(i);
                        items.add(new RSSItemImpl(el.getElementsByTagName("title").item(0).getTextContent(), el.getElementsByTagName("link").item(0).getTextContent(), el.getElementsByTagName("description").item(0).getTextContent()));
                    }
                }
                rssItems = new RSSImpl(url, title, items);
                
            } catch (SAXException e) {
                logger.error("Exception occurred during parsing the RSS feed", e);
            } catch (IOException e) {
                logger.error("Exception occurred during fetching the RSS feed", e);
            }
        } catch (ParserConfigurationException e) {
            logger.error("Exception occurred during parsing the RSS feed", e);
        }

        if (rssItems == null) {
            rssItems = new RSSImpl();
        }
        return rssItems;
    }

    /**
     * Get the feed urls from Cassandra
     */
    public List<String> getSubscribedUrls(String uname) {
        OperationResult<ColumnList<String>> response = null;
        try {
            response = getKeyspace().prepareQuery(CF_SUBSCRIPTIONS).getKey(uname).execute();
        } catch (NotFoundException e) {
            logger.error("No record found for this user: " + uname);
        } catch (Exception t) {
            logger.error("Exception occurred when fetching from Cassandra: " + t);
        }

        final List<String> items = new ArrayList<String>();
        if (response != null) {
            final ColumnList<String> columns = response.getResult();
            for (Column<String> column : columns) {
                items.add(column.getName());
            }
        }
        
        return items;
    }

    /**
     * Add feed url into Cassandra
     */
    public void subscribeUrl(String uname, String url) {
        try {
            OperationResult<Void> opr = getKeyspace().prepareColumnMutation(CF_SUBSCRIPTIONS, uname, url)                                          
                                          .putValue("1", null).execute();

            // TODO: Add latency metrics to Servo
            logger.info("Time taken to add to Cassandra (in ms): " + opr.getLatency(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            logger.error("Exception occurred when writing to Cassandra: " + e);
        }
    }

    /**
     * Delete feed url from Cassandra
     */
    public void unsubscribeUrl(String uname, String url) {
        try {
            OperationResult<Void> opr = getKeyspace().prepareColumnMutation(CF_SUBSCRIPTIONS, uname, url)
                                          .deleteColumn().execute();

            // TODO: Add latency metrics to Servo
            logger.info("Time taken to delete from Cassandra (in ms): " + opr.getLatency(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            logger.error("Exception occurred when writing to Cassandra: " + e);
        }
    }

    /**
     * Connect to Cassandra
     */
    private static Keyspace getKeyspace() {
        if (ks == null) {
            try {
            AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                .forKeyspace("RSS")
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()      
                    .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
                    .setPort(7102)
                    .setMaxConnsPerHost(3)
                    .setSeeds("ec2-54-234-8-47.compute-1.amazonaws.com:7102")
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());

            context.start();
            ks = context.getEntity();
            } catch (Throwable t) {
                logger.error("Exception occurred when initializing Cassandra keyspace: " + t);
            }
        }

        return ks;
    }
}