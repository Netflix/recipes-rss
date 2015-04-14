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
package com.netflix.recipes.rss.netty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * NettyServer and Builder
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public final class NettyServer implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	public static final int cpus = Runtime.getRuntime().availableProcessors();

	static {
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable exc) {
				logger.error("Thread Exc {}", thread.getName(), exc);
				for (Throwable exc2 = exc; exc2 != null; exc2 = exc2.getCause()) {
					if (exc2 instanceof OutOfMemoryError)
						throw new RuntimeException("OutOfMemoryError");
				}
			}
		});
	}

	/**
	 * @return Builder object which will help build the client and server
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String host;
		private int port = 0; // default is any port

		private Map<String, ChannelHandler> handlers = Maps.newHashMap();

		private int numBossThreads = cpus; // IO boss threads
		private int numWorkerThreads = cpus * 4; // worker threads

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder addHandler(String name, ChannelHandler handler) {
			Preconditions.checkNotNull(handler);
			handlers.put(name, handler);
			return this;
		}

		public Builder numBossThreads(int numBossThreads) {
			this.numBossThreads = numBossThreads;
			return this;
		}

		public Builder numWorkerThreads(int numWorkerThreads) {
			this.numWorkerThreads = numWorkerThreads;
			return this;
		}

		/**
		 * Builds and starts netty
		 */
		public ChannelFuture build() {
			EventLoopGroup bossGroup = new NioEventLoopGroup(this.numBossThreads);
			EventLoopGroup workerGroup = new NioEventLoopGroup(this.numWorkerThreads);

			ServerBootstrap serverBootstrap = new ServerBootstrap();

			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HttpResponseEncoder());
							ch.pipeline().addLast(new HttpRequestDecoder());
							ch.pipeline().addLast(new HttpObjectAggregator(1048576));
							for (String name : handlers.keySet()) {
								ch.pipeline().addLast(handlers.get(name));
							}
						}
					});

			ChannelFuture serverChannelFuture = serverBootstrap.bind(new InetSocketAddress(host, port));
			logger.info("Started netty server {}:{}", host, port);

			return serverChannelFuture;
		}
	}

	public void close() {
	}

	private NettyServer() {
	}
}