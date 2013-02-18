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
package com.netflix.recipes.rss;

public class RSSConstants {
	// Edge constants
	public static final String EDGE_WEB_RESOURCE_ROOT_PATH = "edge";
	public static final String EDGE_WEB_RESOURCE_GET_PATH  = "get";

	// Middletier constants
	public static final String MIDDLETIER_EUREKA_SERVICE_NAME = "middletier";

    // Hystrix
	public static final String MIDDLETIER_HYSTRIX_THREAD_POOL = "MiddleTierThreadPool";
    public static final String HYSTRIX_RSS_THREAD_POOL        = "RSSThreadPool";
    public static final String HYSTRIX_RSS_MUTATIONS_GROUP    = "RSSMutationsGroup";
    public static final String HYSTRIX_RSS_GET_GROUP          = "RSSGetGroup";
    public static final String HYSTRIX_RSS_ADD_COMMAND_KEY    = "RSSAdd";
    public static final String HYSTRIX_RSS_DEL_COMMAND_KEY    = "RSSDel";
    public static final String HYSTRIX_RSS_GET_COMMAND_KEY    = "RSSGet";
    public static final String HYSTRIX_STREAM_PATH            = "/hystrix.stream";

	public static final String MIDDLETIER_WEB_RESOURCE_ROOT_PATH = "middletier";
	public static final String MIDDLETIER_WEB_RESOURCE_GET_PATH  = "get";
    
    // Rest Client
    public static final String MIDDLETIER_REST_CLIENT = "middletier-client";

    // Default user name
    public static final String DEFUALT_USER = "default";

    // REST Entry points
    public static final String RSS_ENTRY_POINT = "/rss/user/" + DEFUALT_USER;

    // RSS Store
    public static final String RSS_STORE           = "rss.store";
    public static final String RSS_STORE_CASSANDRA = "cassandra";
    public static final String RSS_STORE_INMEMORY  = "inmemory";

    // Cassandra meta data
    public static final String CASSANDRA_HOST            = "cassandra.host";
    public static final String CASSANDRA_PORT            = "cassandra.port";
    public static final String CASSANDRA_MAXCONNSPERHOST = "cassandra.maxConnectionsPerHost";
    public static final String CASSANDRA_KEYSPACE        = "cassandra.keyspace";
    public static final String CASSANDRA_COLUMNFAMILY    = "cassandra.columnfamily";

    // Jetty
    public static final String JETTY_HTTP_PORT = "jetty.http.port";
    public static final String WEBAPPS_DIR     = "rss-edge/webapp";
}