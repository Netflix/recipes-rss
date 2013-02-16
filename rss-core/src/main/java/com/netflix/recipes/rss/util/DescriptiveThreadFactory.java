package com.netflix.recipes.rss.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cfregly
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
