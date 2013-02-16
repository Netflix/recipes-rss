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

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.recipes.rss.AppConfiguration;
import com.netflix.recipes.rss.netty.NettyHandlerContainer;
import com.netflix.recipes.rss.netty.NettyServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Netty Server
 */
public class BaseNettyServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseNettyServer.class);

	public NettyServer server;
	public final String host;
	public final int port;

	protected final AppConfiguration config;

	@Inject
	public BaseNettyServer(AppConfiguration config) {
		this.config = config;
		this.host = config.getString("netty.host", null);
		this.port = config.getInt("netty.http.port", Integer.MIN_VALUE);
	}

	public void start() {
		LoggingConfiguration.getInstance().configure();

		PackagesResourceConfig rcf = new PackagesResourceConfig(config.getString("jersey.resources.package", "not-found-in-configuration"));

		server = NettyServer
				.builder()
				.host(host)
				// listen on any interface
				.port(port)
				.addHandler(
						"jerseyHandler",
						ContainerFactory.createContainer(
								NettyHandlerContainer.class, rcf))
				.numBossThreads(NettyServer.cpus)
				.numWorkerThreads(NettyServer.cpus * 4).build();

		// Register instance with eureka
		MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();

		ApplicationInfoManager.getInstance().initComponent(instanceConfig);
		ApplicationInfoManager.getInstance().setInstanceStatus(
				InstanceStatus.UP);

		DiscoveryManager.getInstance().initComponent(instanceConfig, new DefaultEurekaClientConfig());
	}

	@Override
	public void close() {
		Closeables.closeQuietly(server);
		LoggingConfiguration.getInstance().stop();
	}
}