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

import com.netflix.zuul.http.HttpServletRequestWrapper;

/**
 * A Servlet 3.0 compliant wrapper.
 */
class Servlet30RequestWrapper extends HttpServletRequestWrapper {

	private HttpServletRequest request;

	Servlet30RequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;
	}

	/**
	 * There is a bug in zuul 1.2.2 where HttpServletRequestWrapper.getRequest returns a
	 * wrapped request rather than the raw one.
	 * @return the original HttpServletRequest
	 */
	@Override
	public HttpServletRequest getRequest() {
		// 之前的返回的Request不是原生的HttpServletRequest, 所以在spring cloud netflix中重写了Servlet30RequestWrapper的getRequest()方法
		// 之前返回的是 return new HttpServletRequestWrapper(req, contentData, parameters);
		return this.request;
	}

}
