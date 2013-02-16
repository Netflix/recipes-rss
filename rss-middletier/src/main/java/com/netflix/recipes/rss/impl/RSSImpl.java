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
package com.netflix.recipes.rss.impl;

import com.netflix.recipes.rss.RSS;
import com.netflix.recipes.rss.RSSItem;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RSSImpl implements RSS{

    private final String url;
    private final String title;
    private final List<RSSItem> items;
    
    public RSSImpl() {
        this.url   = null;
        this.title = null;
        this.items = null;
    }
    
    public RSSImpl(String url, String title, List<RSSItem> items) {
        this.url   = url;
        this.title = title;
        this.items = items;
    }

    @XmlElement(name="url")
    public String getUrl() {
        return url;
    }
    
    @XmlElement(name="title")
    public String getTitle() {
        return title;
    }
    
    @XmlElements({@XmlElement(name="items", type=RSSItemImpl.class)})
    public List<RSSItem> getItems() {
        return items;
    }
}
