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
import com.google.inject.Injector;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.recipes.rss.AppConfiguration;
import com.netflix.recipes.rss.util.RSSModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class MiddleTierServer extends BaseNettyServer {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierServer.class);
    
    @Inject
    public MiddleTierServer(AppConfiguration config) {
    	super(config);
    }
    
    public void start() {
        super.start();
        
        waitUntilFullyRegisteredWithEureka();        
    }

    private void waitUntilFullyRegisteredWithEureka() {
        // use applicationId as the vipAddress
        String vipAddress = System.getProperty("archaius.deployment.applicationId");

        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                .getDiscoveryClient()
                .getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                System.out
                .println("Waiting for service to register with eureka..");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread interrupted while sleeping during the wait for eureka service registration.  This is probably OK.");
                }
            }
        }
        
        logger.info("Registered middletier service with eureka: [{}]", nextServerInfo);
    }

    private void unRegisterWithEureka() {
        // Un register from eureka.
        DiscoveryManager.getInstance().shutdownComponent();
    }

    @Override
    public void close() {
    	super.close();

    	unRegisterWithEureka();
    }
        
    public static void main(String args[]) throws Exception {
    	System.setProperty("archaius.deployment.applicationId", "middletier");

    	Injector injector = LifecycleInjector.builder().withModules(new RSSModule()).createInjector();

    	LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
    	lifecycleManager.start();
    	
    	MiddleTierServer middleTierServer = injector.getInstance(MiddleTierServer.class);
    	middleTierServer.start();
    }
}
