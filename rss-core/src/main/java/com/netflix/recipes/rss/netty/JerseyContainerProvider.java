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
package com.netflix.recipes.rss.netty;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This class is referenced in the following jersey configuration file:
 * 
 * src/main/resources/META-INF/services/com.sun.jersey.spi.container.
 * ContainerProvider
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public class JerseyContainerProvider implements ContainerProvider<NettyHandlerContainer> {

	public NettyHandlerContainer createContainer(Class<NettyHandlerContainer> clazz, ResourceConfig config,WebApplication webApp)
            throws ContainerException {
		if (clazz != NettyHandlerContainer.class) {
			return null;
		}
		return new NettyHandlerContainer(webApp, config);
	}
}