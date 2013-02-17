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
import com.netflix.karyon.spi.PropertyNames;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class MiddleTierServer extends BaseNettyServer {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierServer.class);
    
    public MiddleTierServer() {
    }
    
    public void start() {
        super.start();
        
        waitUntilFullyRegisteredWithEureka();        
    }

    private void waitUntilFullyRegisteredWithEureka() {
        // using applicationId as the vipAddress
        String vipAddress = System.getProperty("archaius.deployment.applicationId");

        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                .getDiscoveryClient()
                .getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                logger.info("Waiting for service to register with eureka...");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread interrupted while sleeping during the wait for eureka service registration.  This is probably OK.");
                }
            }
        }
        
        logger.info("Registered middletier service with eureka: [{}]", nextServerInfo);
    }
        
    public static void main(String args[]) throws Exception {
    	System.setProperty("archaius.deployment.applicationId", "middletier");
    	System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.netflix");
    	
    	MiddleTierServer middleTierServer = new MiddleTierServer();
    	middleTierServer.start();
    }
}
