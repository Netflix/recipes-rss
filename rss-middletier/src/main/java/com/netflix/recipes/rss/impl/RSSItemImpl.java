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

import com.netflix.recipes.rss.RSSItem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RSSItemImpl implements RSSItem {

    private final String title;
    private final String link;
    private final String description;
    
    public RSSItemImpl() {
        this.title = null;
        this.link = null;
        this.description = null;                
    }
    
    public RSSItemImpl(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }
    
    @XmlElement(name="title")
    public String getTitle() {
        return title;
    }
    
    @XmlElement(name="link")
    public String getLink() {
        return link;
    }
    
    @XmlElement(name="description")
    public String getDescription() {
        return description;
    }
}
