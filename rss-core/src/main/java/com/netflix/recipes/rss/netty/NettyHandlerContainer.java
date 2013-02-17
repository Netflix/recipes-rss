/**
 * The MIT License
 *
 * Copyright (c) 2009 Carl Bystrom
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Snagged from the following RenderShark component:
 * https://code.google.com/p/rendershark
 * /source/browse/trunk/rendershark/src/main
 * /java/com/sun/jersey/server/impl/container/netty/NettyHandlerContainer.java
 *
 */
package com.netflix.recipes.rss.netty;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Sharable
public class NettyHandlerContainer extends SimpleChannelUpstreamHandler {
	public static final String PROPERTY_BASE_URI = "com.sun.jersey.server.impl.container.netty.baseUri";

	private final WebApplication application;
	private final String baseUri;

	NettyHandlerContainer(WebApplication application, ResourceConfig resourceConfig) {
		this.application = application;
		this.baseUri = (String) resourceConfig.getProperty(PROPERTY_BASE_URI);
	}

	private final static class Writer implements ContainerResponseWriter {
		private final Channel channel;
		private HttpResponse response;

		private Writer(Channel channel) {
			this.channel = channel;
		}

		public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse cResponse) throws IOException {

			response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.valueOf(cResponse.getStatus()));
			for (Map.Entry<String, List<Object>> e : cResponse.getHttpHeaders().entrySet()) {
				List<String> values = new ArrayList<String>();
				for (Object v : e.getValue())
					values.add(ContainerResponse.getHeaderValue(v));
				response.setHeader(e.getKey(), values);
			}
			ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
			response.setContent(buffer);
			return new ChannelBufferOutputStream(buffer);
		}

		public void finish() throws IOException {
			// Streaming is not supported. Entire response will be written
			// downstream once finish() is called.
			channel.write(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext context, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		String base = getBaseUri(request);
		URI baseUri = new URI(base);
		URI requestUri = new URI(base.substring(0, base.length() - 1) + request.getUri());
		ContainerRequest cRequest = new ContainerRequest(application, request
				.getMethod().getName(), baseUri, requestUri,
				getHeaders(request), new ChannelBufferInputStream(
						request.getContent()));
		application.handleRequest(cRequest, new Writer(e.getChannel()));
	}

	private String getBaseUri(HttpRequest request) {
		if (baseUri != null) {
			return baseUri;
		}

		return "http://" + request.getHeader(HttpHeaders.Names.HOST) + "/";
	}

	private InBoundHeaders getHeaders(HttpRequest request) {
		InBoundHeaders headers = new InBoundHeaders();
		for (String name : request.getHeaderNames()) {
			headers.put(name, request.getHeaders(name));
		}
		return headers;
	}
}
