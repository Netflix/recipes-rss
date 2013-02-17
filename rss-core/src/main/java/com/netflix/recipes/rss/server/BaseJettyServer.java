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

import com.google.inject.Inject;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.recipes.rss.AppConfiguration;
import com.netflix.recipes.rss.util.InetAddressUtils;
import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Base Jetty Server
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseJettyServer implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(BaseJettyServer.class);

	public final Server server;
	public final String host;
	public final int port;
	protected final AppConfiguration config;

	@Inject
	public BaseJettyServer(AppConfiguration config) {
		this.config = config;

		server = new Server();

		port = config.getInt("jetty.http.port", Integer.MIN_VALUE);
		host = InetAddressUtils.getBestReachableIp();
	}

	public void start() {
		LoggingConfiguration.getInstance().configure();

		final Context context = new Context(server, "/", Context.SESSIONS);
		context.setResourceBase("webapp");
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.addServlet(JspServlet.class, "*.jsp");
		context.addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream");

		final Server server = new Server(port);
		server.setHandler(context);

		try {
			server.start();
		} catch (Exception exc) {
			logger.error("Error starting jetty.", exc);
		}

		// Enable eureka client functionality
		MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();

		ApplicationInfoManager.getInstance().initComponent(instanceConfig);
		ApplicationInfoManager.getInstance().setInstanceStatus(
				InstanceStatus.UP);

		DiscoveryManager.getInstance().initComponent(instanceConfig, new DefaultEurekaClientConfig());
	}

	@Override
	public void close() {
		try {
			server.stop();
		} catch (Exception exc) {
			logger.error("Error stopping jetty.", exc);
		}
		LoggingConfiguration.getInstance().stop();
	}
}