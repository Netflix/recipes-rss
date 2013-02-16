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
package com.netflix.recipes.rss.hystrix;

import com.google.common.base.Charsets;
import com.netflix.client.ClientFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.niws.client.http.HttpClientRequest;
import com.netflix.niws.client.http.HttpClientRequest.Verb;
import com.netflix.niws.client.http.HttpClientResponse;
import com.netflix.niws.client.http.RestClient;
import com.netflix.recipes.rss.RSSConstants;
import org.apache.commons.io.IOUtils;

import java.net.URI;

/**
 * Calls the middle tier Get RSS entry point
 */
public class GetRSSCommand extends HystrixCommand<String> {
	public GetRSSCommand() {
        super (
            Setter.withGroupKey(
                HystrixCommandGroupKey.Factory.asKey(RSSConstants.HYSTRIX_RSS_GET_GROUP))
                .andCommandKey(HystrixCommandKey.Factory.asKey(RSSConstants.HYSTRIX_RSS_GET_COMMAND_KEY))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(RSSConstants.HYSTRIX_RSS_THREAD_POOL)
            )
        );
	}

	@Override
	protected String run() {
		try {
			// The named client param must match the prefix for the ribbon
			// configuration specified in the edge.properties file
			RestClient client = (RestClient) ClientFactory.getNamedClient(RSSConstants.MIDDLETIER_REST_CLIENT);

			HttpClientRequest request = HttpClientRequest
					.newBuilder()
					.setVerb(Verb.GET)
					.setUri(new URI("/"
							+ RSSConstants.MIDDLETIER_WEB_RESOURCE_ROOT_PATH
							+ RSSConstants.RSS_ENTRY_POINT)
                    )
					.build();
			HttpClientResponse response = client.executeWithLoadBalancer(request);

			return IOUtils.toString(response.getRawEntity(), Charsets.UTF_8);
		} catch (Exception exc) {
			throw new RuntimeException("Exception", exc);
		}
	}

	@Override
	protected String getFallback() {
        // Empty json
		return "{}";
	}
}