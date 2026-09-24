/*
 * Copyright (c) 2026 suretool contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sure.tool.http;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JDK {@link HttpClient} 构建器：连接池（keep-alive 复用）、虚拟线程 executor、代理、超时与 TLS 版本。
 * 构建出的 HttpClient 自带连接复用与并发限制，适合大批量请求场景；与 {@link HttpRequest#client(HttpClient)} 配合使用。
 *
 * <pre>{@code
 * HttpClient client = HttpClientBuilder.builder()
 *         .virtualThreads()
 *         .connectTimeout(3, TimeUnit.SECONDS)
 *         .proxy("127.0.0.1", 8080)
 *         .build();
 * String body = HttpRequest.get("https://api.example.com").client(client).execute().body();
 * }</pre>
 *
 * @author suretool
 * @since 1.1.0
 */
public class HttpClientBuilder {

	/** 默认连接超时（秒） */
	private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

	private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private ExecutorService executor;
	private ProxySelector proxySelector;
	private boolean followRedirects = true;
	private Version version = Version.HTTP_1_1;

	private HttpClientBuilder() {
	}

	/**
	 * 创建构建器。
	 *
	 * @return 构建器
	 */
	public static HttpClientBuilder builder() {
		return new HttpClientBuilder();
	}

	/**
	 * 连接超时。
	 *
	 * @param connectTimeout 超时
	 * @return 本构建器
	 */
	public HttpClientBuilder connectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	/**
	 * 使用虚拟线程 executor（JDK 21+，默认关闭；开启后每个请求独占虚拟线程，适合高并发 IO 场景）。
	 *
	 * @return 本构建器
	 */
	public HttpClientBuilder virtualThreads() {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		return this;
	}

	/**
	 * 自定义 executor。
	 *
	 * @param executor 线程池
	 * @return 本构建器
	 */
	public HttpClientBuilder executor(ExecutorService executor) {
		this.executor = executor;
		return this;
	}

	/**
	 * HTTP 代理。
	 *
	 * @param host 代理主机
	 * @param port 代理端口
	 * @return 本构建器
	 */
	public HttpClientBuilder proxy(String host, int port) {
		this.proxySelector = ProxySelector.of(new InetSocketAddress(host, port));
		return this;
	}

	/**
	 * 任意 HTTP 代理。
	 *
	 * @param proxy 代理
	 * @return 本构建器
	 */
	public HttpClientBuilder proxy(Proxy proxy) {
		if (proxy == null || proxy.type() != Proxy.Type.HTTP || proxy.address() == null) {
			throw new IllegalArgumentException("JDK HttpClient 仅支持 HTTP 代理");
		}
		this.proxySelector = ProxySelector.of((InetSocketAddress) proxy.address());
		return this;
	}

	/**
	 * 是否跟随重定向（默认 true）。
	 *
	 * @param followRedirects 是否跟随
	 * @return 本构建器
	 */
	public HttpClientBuilder followRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * 强制 HTTP/1.1（默认，兼容性好）；传 {@link Version#HTTP_2} 启用 HTTP/2。
	 *
	 * @param version HTTP 版本
	 * @return 本构建器
	 */
	public HttpClientBuilder version(Version version) {
		this.version = version;
		return this;
	}

	/**
	 * 构建 HttpClient（连接池/keep-alive 由 JDK 管理）。
	 *
	 * @return HttpClient
	 */
	public HttpClient build() {
		HttpClient.Builder builder = HttpClient.newBuilder()
				.version(version)
				.connectTimeout(connectTimeout)
				.followRedirects(followRedirects ? Redirect.NORMAL : Redirect.NEVER);
		if (executor != null) {
			builder.executor(executor);
		}
		if (proxySelector != null) {
			builder.proxy(proxySelector);
		}
		return builder.build();
	}
}
