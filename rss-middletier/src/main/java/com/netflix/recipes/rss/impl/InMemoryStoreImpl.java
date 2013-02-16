package com.netflix.recipes.rss.impl;

import com.netflix.recipes.rss.RSSStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryStoreImpl implements RSSStore {

    // Maintain the same data model as Cassandra
    private static final Map<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();

    @Override
    public List<String> getSubscribedUrls(String userId) {
        List<String> urls = new ArrayList<String>();
        if (map.containsKey(userId)) {
            for (final Map.Entry<String, String> entry: map.get(userId).entrySet()) {
                urls.add(entry.getKey());
            }
        }

        return urls;
    }

    @Override
    public void subscribeUrl(String userId, String url) {
        HashMap<String, String> feeds;
        if (map.containsKey(userId)) {
            feeds = map.get(userId);
        } else {
            feeds = new HashMap<String, String>(1);
        }
        feeds.put(url, "1");
        map.put(userId, feeds);
    }

    @Override
    public void unsubscribeUrl(String userId, String url) {
        map.get(userId).remove(url);
    }
}
