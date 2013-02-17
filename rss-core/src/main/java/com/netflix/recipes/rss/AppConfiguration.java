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
package com.netflix.recipes.rss;

import java.io.Closeable;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public interface AppConfiguration extends Closeable {

	public String getString(String key, String defaultValue);

	public int getInt(String key, int defaultValue);

	public long getLong(String key, int defaultValue);

	public boolean getBoolean(String key, boolean defaultValue);

	/**
	 * Sets an instance-level override. This will trump everything including
	 * dynamic properties and system properties. Useful for tests.
	 * 
	 * @param key
	 * @param value
	 */
	public void setOverrideProperty(String key, Object value);
}