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
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.recipes.rss.AppConfiguration;
import com.netflix.recipes.rss.util.RSSModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Edge Server
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public class EdgeServer extends BaseJettyServer {
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeServer.class);

	@Inject
	public EdgeServer(AppConfiguration config) {
		super(config);

		// populate the eureka-specific properties
		System.setProperty("eureka.client.props", ConfigurationManager
				.getDeploymentContext().getApplicationId());
		System.setProperty("eureka.environment", ConfigurationManager
				.getDeploymentContext().getDeploymentEnvironment());
	}

	public static void main(final String[] args) throws Exception {
		System.setProperty("archaius.deployment.applicationId", "edge");

		Injector injector = LifecycleInjector.builder()
				.withModules(new RSSModule()).createInjector();

		LifecycleManager lifecycleManager = injector
				.getInstance(LifecycleManager.class);
		lifecycleManager.start();

		EdgeServer edgeServer = injector.getInstance(EdgeServer.class);
		edgeServer.start();
	}
}