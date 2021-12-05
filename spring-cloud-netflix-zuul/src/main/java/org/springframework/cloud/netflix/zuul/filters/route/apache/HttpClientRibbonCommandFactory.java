/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.zuul.filters.route.apache;

import java.util.Collections;
import java.util.Set;

import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.ribbon.apache.RibbonLoadBalancingHttpClient;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommandFactory;

/**
 * @author Christian Lohmann
 * @author Ryan Baxter
 */
public class HttpClientRibbonCommandFactory extends AbstractRibbonCommandFactory {

	private final SpringClientFactory clientFactory;

	private final ZuulProperties zuulProperties;

	public HttpClientRibbonCommandFactory(SpringClientFactory clientFactory,
			ZuulProperties zuulProperties) {
		this(clientFactory, zuulProperties, Collections.<FallbackProvider>emptySet());
	}

	public HttpClientRibbonCommandFactory(SpringClientFactory clientFactory,
			ZuulProperties zuulProperties, Set<FallbackProvider> fallbackProviders) {
		super(fallbackProviders);
		this.clientFactory = clientFactory;
		this.zuulProperties = zuulProperties;
	}

	@Override
	public HttpClientRibbonCommand create(final RibbonCommandContext context) {
		FallbackProvider zuulFallbackProvider = getFallbackProvider(
				context.getServiceId());

		// 获取服务名
		final String serviceId = context.getServiceId();
		// 从某个服务的Spring上下文获取RibbonLoadBalancingHttpClient这个Bean, 默认是HttpClientRibbonConfiguration创建的共用的Bean
		final RibbonLoadBalancingHttpClient client = this.clientFactory
				.getClient(serviceId, RibbonLoadBalancingHttpClient.class);
		// 从某个服务的Spring上下文获取ILoadBalancer这个Bean, 就是Ribbon的逻辑了
		client.setLoadBalancer(this.clientFactory.getLoadBalancer(serviceId));

		// 父类的父类是HystrixCommand
		return new HttpClientRibbonCommand(serviceId, client, context, zuulProperties,
				zuulFallbackProvider, clientFactory.getClientConfig(serviceId));
	}

}
