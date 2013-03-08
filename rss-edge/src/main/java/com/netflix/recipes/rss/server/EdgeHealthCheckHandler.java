package com.netflix.recipes.rss.server;

import com.netflix.karyon.spi.HealthCheckHandler;

public class EdgeHealthCheckHandler implements HealthCheckHandler {

    public int getStatus() {
        return 200;
    }

}
