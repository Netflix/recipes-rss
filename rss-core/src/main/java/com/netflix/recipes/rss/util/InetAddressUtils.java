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
package com.netflix.recipes.rss.util;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Utils for various ip-related functionality
 * 
 * @author Chris Fregly (chris@fregly.com)
 *  
 */ 
public class InetAddressUtils {
	private static final Logger logger = LoggerFactory
			.getLogger(InetAddressUtils.class);

	private static Random random = new Random();

	public static String getBestReachableIp() {
		Collection<InetAddress> ipCollection;
		try {
			ipCollection = getLocalIPCollection();
			if (ipCollection.size() > 0) {
				// https://github.com/Netflix/curator/issues/230
				Iterator<InetAddress> addressIter = ipCollection.iterator();
				logger.trace("found the following local ips:");
				while (addressIter.hasNext()) {
					InetAddress address = (InetAddress) addressIter.next();
					try {
						boolean isReachable = address.isReachable(500);
						boolean isIPV6 = address instanceof Inet6Address;
						logger.trace("  " + address.getHostAddress()
								+ " - Reachable? " + address.isReachable(3000));
						if (isReachable && !isIPV6) {
							return address.getHostAddress();
						}
					} catch (Exception exc) {
						logger.trace("Could not find the IP is reachable. Trying the next one..");
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException("Cannot determine local ip.", e);
		}

		throw new RuntimeException("Cannot determine local ip.");
	}

	/**
	 * based on http://pastebin.com/5X073pUc
	 * <p>
	 * 
	 * Returns all available IP addresses.
	 * <p>
	 * In error case or if no network connection is established, we return an
	 * empty list here.
	 * <p>
	 * Loopback addresses are excluded - so 127.0.0.1 will not be never
	 * returned.
	 * <p>
	 * The "primary" IP might not be the first one in the returned list.
	 * 
	 * @return Returns all IP addresses (can be an empty list in error case or
	 *         if network connection is missing).
	 * @since 0.1.0
	 * @throws SocketException
	 *             errors
	 */
	public static Collection<InetAddress> getLocalIPCollection()
			throws SocketException {
		List<InetAddress> addressList = Lists.newArrayList();
		Enumeration<NetworkInterface> nifs = NetworkInterface
				.getNetworkInterfaces();
		if (nifs == null) {
			return addressList;
		}

		while (nifs.hasMoreElements()) {
			NetworkInterface nif = nifs.nextElement();

			// We ignore subinterfaces - as not yet needed.

			Enumeration<InetAddress> adrs = nif.getInetAddresses();
			while (adrs.hasMoreElements()) {
				InetAddress adr = adrs.nextElement();
				if (adr != null && !adr.isLoopbackAddress()
						&& (nif.isPointToPoint() || !adr.isLinkLocalAddress())) {
					addressList.add(adr);
				}
			}
		}
		return addressList;
	}

	/**
	 * Generate a random port between (1024, 65536) exclusive
	 * 
	 * @return the randomly-generated port
	 */
	public static int generateRandomPort() {
		// keep from generating any port < 1024
		return random.nextInt(65536 - 1024 + 1) + 1024;
	}
}