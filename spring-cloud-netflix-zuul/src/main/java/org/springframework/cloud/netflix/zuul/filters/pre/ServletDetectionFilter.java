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

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ZuulServlet;

import org.springframework.web.servlet.DispatcherServlet;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.IS_DISPATCHER_SERVLET_REQUEST_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVLET_DETECTION_FILTER_ORDER;

/**
 * Detects whether a request is ran through the {@link DispatcherServlet} or
 * {@link ZuulServlet}. The purpose was to detect this up-front at the very beginning of
 * Zuul filter processing and rely on this information in all filters. RequestContext is
 * used such that the information is accessible to classes which do not have a request
 * reference.
 *
 * @author Adrian Ivan
 */
public class ServletDetectionFilter extends ZuulFilter {

	public ServletDetectionFilter() {
	}

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	/**
	 * Must run before other filters that rely on the difference between DispatcherServlet
	 * and ZuulServlet.
	 */
	@Override
	public int filterOrder() {
		return SERVLET_DETECTION_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		return true;
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
		HttpServletRequest request = ctx.getRequest();
		if (!(request instanceof HttpServletRequestWrapper)
				&& isDispatcherServletRequest(request)) {
			// 往RequestContext设置isDispatcherServletRequest=true, 就没做什么了
			ctx.set(IS_DISPATCHER_SERVLET_REQUEST_KEY, true);
		}
		else {
			// 往RequestContext设置isDispatcherServletRequest=false, 就没做什么了
			ctx.set(IS_DISPATCHER_SERVLET_REQUEST_KEY, false);
		}

		return null;
	}

	private boolean isDispatcherServletRequest(HttpServletRequest request) {
		return request.getAttribute(
				DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null;
	}

}
