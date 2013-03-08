package com.netflix.recipes.rss.manager;

import com.netflix.karyon.spi.HealthCheckHandler;

public class MiddleTierHealthCheckHandler implements HealthCheckHandler {

    public int getStatus() {
        return RSSManager.getInstance().getStatus();
    }

}
