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
package com.netflix.recipes.rss.test;

import java.util.Arrays;
import java.util.List;

public class BaseAppTest {
	public enum Option {
	}

	private static List<Option> optionsList;

	public BaseAppTest() {
	}

	public static void baseSetUp(Option... theOptions) throws Exception {
		if (theOptions == null) {
			return;
		}

		optionsList = Arrays.asList(theOptions);
	}

	public static void baseTearDown() throws Exception {
		if (optionsList == null) {
			return;
		}
	}
}