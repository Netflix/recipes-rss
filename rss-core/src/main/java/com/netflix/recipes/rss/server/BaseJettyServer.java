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
package com.netflix.recipes.rss.server;

import java.io.Closeable;

import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.karyon.server.KaryonServer;

/**
 * Base Jetty Server
 *
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseJettyServer implements Closeable {
    static {
        LoggingConfiguration.getInstance().configure();
    }

    private static final Logger logger = LoggerFactory.getLogger(BaseJettyServer.class);

    private final Server jettyServer;
    private final KaryonServer karyonServer;

    protected final Injector injector;

	private final String portPropertyName;

	private String webAppsDir;

	private String hystrixStreamPath;

    public BaseJettyServer(String portPropertyName, String webAppsDir, String hystrixStreamPath) {

    	System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

    	this.portPropertyName = portPropertyName;
		this.webAppsDir = webAppsDir;
		this.hystrixStreamPath = hystrixStreamPath;

        this.karyonServer = new KaryonServer();
        this.injector     = karyonServer.initialize();
        this.jettyServer  = new Server();
    }

    public void start() {

        final int port   = ConfigurationManager.getConfigInstance().getInt(portPropertyName, Integer.MIN_VALUE);

        final Context context = new Context(jettyServer, "/", Context.SESSIONS);
        context.setResourceBase(webAppsDir);
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.addServlet(JspServlet.class, "*.jsp");

        // Enable hystrix.stream
        context.addServlet(HystrixMetricsStreamServlet.class, hystrixStreamPath);

        final Server server = new Server(port);
        server.setHandler(context);

        try {
            karyonServer.start();
            server.start();
        } catch (Exception exc) {
            throw new RuntimeException("Cannot start karyon server ...", exc);
        }
    }

    public void close() {
        try {
            jettyServer.stop();
            karyonServer.close();
        } catch (Exception exc) {
            logger.error("Error stopping jetty ...", exc);
        }
        LoggingConfiguration.getInstance().stop();
    }
}
