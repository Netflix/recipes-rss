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
package com.netflix.recipes.rss.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adds descriptive thread names for debugging purposes.  Allows priority and daemon to be set, as well.
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public class DescriptiveThreadFactory implements ThreadFactory {
	private final String description;
	private final int priority;
	private final boolean daemon;
	private final AtomicInteger n = new AtomicInteger(1);

	public DescriptiveThreadFactory(String description) {
		this(description, Thread.NORM_PRIORITY, false);
	}

	public DescriptiveThreadFactory(String description, int priority,
			boolean daemon) {
		this.description = description;
		this.priority = priority;
		this.daemon = daemon;
	}

	public Thread newThread(Runnable runnable) {
		String threadDescription = description + "-" + n.getAndIncrement();
		Thread thread = new Thread(runnable, threadDescription);
		thread.setPriority(priority);
		thread.setDaemon(daemon);
		return thread;
	}
}
