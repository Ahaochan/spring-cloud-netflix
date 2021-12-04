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

package org.springframework.cloud.netflix.zuul.filters.pre;

import javax.servlet.http.HttpServletRequest;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.DEBUG_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Pre {@link ZuulFilter} that sets {@link RequestContext} debug attributes to true if the
 * "debug" request parameter is set.
 *
 * @author Spencer Gibb
 */
public class DebugFilter extends ZuulFilter {

	private static final DynamicBooleanProperty ROUTING_DEBUG = DynamicPropertyFactory
			.getInstance().getBooleanProperty(ZuulConstants.ZUUL_DEBUG_REQUEST, false);

	private static final DynamicStringProperty DEBUG_PARAMETER = DynamicPropertyFactory
			.getInstance().getStringProperty(ZuulConstants.ZUUL_DEBUG_PARAMETER, "debug");

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return DEBUG_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		// url参数有?debug=true才能执行这个过滤器
		if ("true".equals(request.getParameter(DEBUG_PARAMETER.get()))) {
			return true;
		}
		// 默认值是false
		return ROUTING_DEBUG.get();
	}

	// ServletDetectionFilter(-3) -> Servlet30WrapperFilter(-2) -> FormBodyWrapperFilter(-1) -> DebugFilter(1) -> PreDecorationFilter(5)
	// ServletDetectionFilter(-3): 往RequestContext设置isDispatcherServletRequest=true, 就没做什么了
	// Servlet30WrapperFilter(-2): 将HttpServletRequest用Servlet30RequestWrapper包装了一下, 装饰模式, 修复之前不能getRequest()不能获取原生HttpServletRequest的bug
	// FormBodyWrapperFilter(-1): 正常不执行, 只处理contentType为application/x-www-form-urlencoded或者multipart/form-data的非get请求, 也是对request做包装
	// DebugFilter(1): 正常不执行, url参数有?debug=true才能执行这个过滤器, 用来开启debug模式, 后面打印一些debug日志
	// PreDecorationFilter(5): 根据uri匹配application.yml中的路由规则, 将对应的配置设置到RequestContext或者请求头里
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		// 设置debug标识, 后续打印debug日志
		ctx.setDebugRouting(true);
		ctx.setDebugRequest(true);
		return null;
	}

}
