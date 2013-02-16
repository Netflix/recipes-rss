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
package com.netflix.recipes.rss.jersey.resources;

import com.netflix.recipes.rss.Subscriptions;
import com.netflix.recipes.rss.manager.RSSManager;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.*;
import com.netflix.servo.stats.StatsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

/**
 * Rest entry points for fetching/adding/deleting RSS feeds.
 * RSS Edge service will be calling these APIs
 */
@Path("/middletier")
public class MiddleTierResource {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierResource.class);

    // JMX:  com.netflix.servo.COUNTER.MiddleTierRSS_*
    private static final Counter getRSSRequestCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_getRequestCounter").build());
    private static final Counter addRSSRequestCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_addRequestCounter").build());
    private static final Counter delRSSRequestCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_delRequestCounter").build());

    // JMX:  com.netflix.servo.COUNTER.MiddleTierRSS_*
    private static final Counter getRSSErrorCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_getErrorCounter").build());
    private static final Counter addRSSErrorCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_addErrorCounter").build());
    private static final Counter delRSSErrorCounter = new BasicCounter(MonitorConfig.builder("MiddleTierRSS_delErrorCounter").build());

    // JMX:  com.netflix.servo.COUNTER.MiddleTierRSS_*
    // JMX:  com.netflix.servo.MiddleTierRSS_* (95th and 99th percentile)
    private static final StatsTimer getRSSStatsTimer = new StatsTimer(MonitorConfig.builder("MiddleTierRSS_getStatsTimer").build(), new StatsConfig.Builder().build());
    private static final StatsTimer addRSSStatsTimer = new StatsTimer(MonitorConfig.builder("MiddleTierRSS_addStatsTimer").build(), new StatsConfig.Builder().build());
    private static final StatsTimer delRSSStatsTimer = new StatsTimer(MonitorConfig.builder("MiddleTierRSS_delStatsTimer").build(), new StatsConfig.Builder().build());

    static {
        DefaultMonitorRegistry.getInstance().register(getRSSRequestCounter);
        DefaultMonitorRegistry.getInstance().register(addRSSRequestCounter);
        DefaultMonitorRegistry.getInstance().register(delRSSRequestCounter);

        DefaultMonitorRegistry.getInstance().register(getRSSErrorCounter);
        DefaultMonitorRegistry.getInstance().register(addRSSErrorCounter);
        DefaultMonitorRegistry.getInstance().register(delRSSErrorCounter);

        DefaultMonitorRegistry.getInstance().register(getRSSStatsTimer);
        DefaultMonitorRegistry.getInstance().register(addRSSStatsTimer);
        DefaultMonitorRegistry.getInstance().register(delRSSStatsTimer);
    }

    public MiddleTierResource() {
    }

    @GET
    @Path("/rss/user/{user}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response fetchSubscriptions (final @PathParam("user") String user) {

        // Start timer
        Stopwatch stopwatch = getRSSStatsTimer.start();

        try {
            getRSSRequestCounter.increment();

            Subscriptions subscriptions = RSSManager.getInstance().getSubscriptions(user);
            return Response.ok(subscriptions).build();
        } catch (Exception e) {
            logger.error("Exception occurred when fetching subscriptions", e);
            getRSSErrorCounter.increment();
            return Response.serverError().build();
        } finally {
            stopwatch.stop();
            getRSSStatsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }

    @POST
    @Path("/rss/user/{user}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response subscribe (
            final @QueryParam("url") String url,
            final @PathParam("user") String user) {

        // Start timer
        Stopwatch stopwatch = addRSSStatsTimer.start();

        try {
            addRSSRequestCounter.increment();
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            RSSManager.getInstance().addSubscription(user, decodedUrl);

            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Exception occurred during subscription", e);
            addRSSErrorCounter.increment();
            return Response.serverError().build();
        } finally {
            stopwatch.stop();
            addRSSStatsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }

    @DELETE
    @Path("/rss/user/{user}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response unsubscribe (
            final @QueryParam("url") String url,
            final @PathParam("user") String user) {

        // Start timer
        Stopwatch stopwatch = delRSSStatsTimer.start();

        try {
            delRSSRequestCounter.increment();
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            RSSManager.getInstance().deleteSubscription(user, decodedUrl);

            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Exception occurred during un-subscription", e);
            delRSSErrorCounter.increment();
            return Response.serverError().build();
        } finally {
            stopwatch.stop();
            delRSSStatsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }
}