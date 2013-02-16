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
import com.netflix.recipes.rss.Subscriptions;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SubscriptionsImpl implements Subscriptions {

    private String user;
    private List<RSS> subscriptions;
    
    public SubscriptionsImpl() {
        this.user = null;
        this.subscriptions = null;
    }
    
    public SubscriptionsImpl(String user, List<RSS> subscriptions) {
        this.user = user;
        this.subscriptions = subscriptions;
    }
    
    @XmlElement(name="user")
    public String getUser() {
        return user;
    }

    @XmlElements({@XmlElement(name="subscriptions", type=RSSImpl.class)})
    public List<RSS> getSubscriptions() {
        return subscriptions;
    }

}
