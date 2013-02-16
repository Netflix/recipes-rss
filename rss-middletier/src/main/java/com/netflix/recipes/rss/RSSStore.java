package com.netflix.recipes.rss;

import java.util.List;

public interface RSSStore {
    List<String> getSubscribedUrls(String userId) throws Exception;

    void subscribeUrl(String userId, String url) throws Exception;

    void unsubscribeUrl(String userId, String url) throws Exception;
}