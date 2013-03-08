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
import com.netflix.recipes.rss.util.DescriptiveThreadFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * NettyServer and Builder
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public final class NettyServer implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	public static final int cpus = Runtime.getRuntime().availableProcessors();

	private ChannelGroup channelGroup = new DefaultChannelGroup();

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

	public String getListenHost() {
		return ((InetSocketAddress) channelGroup.find(1).getLocalAddress()).getHostName();
	}

	public int getListenPort() {
		return ((InetSocketAddress) channelGroup.find(1).getLocalAddress()).getPort();
	}

	public void addChannel(Channel channel) {
		channelGroup.add(channel);
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

		private ChannelHandler encoder = new HttpResponseEncoder();
		private ChannelHandler decoder = new HttpRequestDecoder();

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

		public Builder encoder(ChannelHandler encoder) {
			this.encoder = encoder;
			return this;
		}

		public Builder decoder(ChannelHandler decoder) {
			this.decoder = decoder;
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
		public NettyServer build() {
			PipelineFactory factory = new PipelineFactory(handlers, encoder,
					decoder, numBossThreads);

			ThreadPoolExecutor bossPool = new ThreadPoolExecutor(
					numBossThreads, numBossThreads, 60, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new DescriptiveThreadFactory("Boss-Thread"));

			ThreadPoolExecutor workerPool = new ThreadPoolExecutor(
					numWorkerThreads, numWorkerThreads, 60, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new DescriptiveThreadFactory("Worker-Thread"));

			ChannelFactory nioServer = new NioServerSocketChannelFactory(
					bossPool, workerPool, numWorkerThreads);

			ServerBootstrap serverBootstrap = new ServerBootstrap(nioServer);
			serverBootstrap.setOption("reuseAddress", true);
			serverBootstrap.setOption("keepAlive", true);
			serverBootstrap.setPipelineFactory(factory);

			Channel serverChannel = serverBootstrap.bind(new InetSocketAddress(
					host, port));
			logger.info("Started netty server {}:{}", host, port);

			NettyServer server = new NettyServer();
			server.addChannel(serverChannel);

			return server;
		}
	}

	public static class PipelineFactory implements ChannelPipelineFactory {
		static final String CHANNEL_HANDLERS = "channelHandlers";
		static final String ENCODER_NAME = "encoder";
		static final String DECODER_NAME = "decoder";

		final ChannelHandler executionHandler;
		final Map<String, ChannelHandler> handlers;

		final ChannelHandler encoder;
		final ChannelHandler decoder;

		public PipelineFactory(Map<String, ChannelHandler> handlers,
				ChannelHandler encoder, ChannelHandler decoder, int numThreads) {

			this.handlers = handlers;
			this.encoder = encoder;
			this.decoder = decoder;

			if (numThreads != 0) {
				ThreadPoolExecutor executorThreadPool = new ThreadPoolExecutor(
						NettyServer.cpus, NettyServer.cpus * 4, 60,
						TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
						new DescriptiveThreadFactory("Executor-Thread"));

				this.executionHandler = new ExecutionHandler(executorThreadPool);
			} else {
				this.executionHandler = null;
			}
		}

		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("executionHandler", executionHandler);
			pipeline.addLast(DECODER_NAME, decoder);
			pipeline.addLast(ENCODER_NAME, encoder);

			for (Entry<String, ChannelHandler> handler : handlers.entrySet()) {
				pipeline.addLast(handler.getKey(), handler.getValue());
			}

			return pipeline;
		}
	}

	public static class ClientPipelineFactory extends PipelineFactory {
		public ClientPipelineFactory(Map<String, ChannelHandler> handlers,
				ChannelHandler encoder, ChannelHandler decoder) {

			super(handlers, encoder, decoder, 0);
		}

		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast(DECODER_NAME, decoder);
			pipeline.addLast(ENCODER_NAME, encoder);

			for (Entry<String, ChannelHandler> handler : handlers.entrySet()) {
				pipeline.addLast(handler.getKey(), handler.getValue());
			}

			return pipeline;
		}
	}

	public void close() {
		channelGroup.close();
	}

	private NettyServer() {
	}
}