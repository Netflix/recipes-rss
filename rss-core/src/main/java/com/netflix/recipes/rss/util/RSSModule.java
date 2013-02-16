package com.netflix.recipes.rss.util;

import com.netflix.recipes.rss.AppConfiguration;
import com.netflix.recipes.rss.RSSConfiguration;
import com.google.inject.AbstractModule;

public class RSSModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(AppConfiguration.class).to(RSSConfiguration.class);
	}
}