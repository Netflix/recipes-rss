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

import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.recipes.rss.netty.NettyHandlerContainer;
import com.netflix.recipes.rss.netty.NettyServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseNettyServer implements Closeable {
    static {
        LoggingConfiguration.getInstance().configure();
    }

    public NettyServer nettyServer;
    public final KaryonServer karyonServer;

    public String host;
    public int port;

    protected final Injector injector;

    //protected AppConfiguration config;

    public BaseNettyServer() {

        // This must be set before karyonServer.initialize() otherwise the
        // archaius properties will not be available in JMX/jconsole
        System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

        this.karyonServer = new KaryonServer();
        this.injector = karyonServer.initialize();		
    }

    public void start() {
        this.host = ConfigurationManager.getConfigInstance().getString("netty.http.host", "not-found-in-configuration");
        this.port = ConfigurationManager.getConfigInstance().getInt("netty.http.port", Integer.MIN_VALUE);

        final PackagesResourceConfig rcf = new PackagesResourceConfig(ConfigurationManager.getConfigInstance().getString("jersey.resources.package","not-found-in-configuration"));

        nettyServer = NettyServer
                .builder()
                .host(host)
                .port(port)
                .addHandler(
                        "jerseyHandler",
                        ContainerFactory.createContainer(
                                NettyHandlerContainer.class, rcf))
                                .numBossThreads(NettyServer.cpus)
                                .numWorkerThreads(NettyServer.cpus * 4).build();
        try {
            karyonServer.start();
        } catch (Exception exc) {
            throw new RuntimeException("Cannot start karyon server.", exc);
        }
    }

    public void close() {
        Closeables.closeQuietly(nettyServer);
        Closeables.closeQuietly(karyonServer);
        LoggingConfiguration.getInstance().stop();
    }
}