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
/**
 * sure-aop：零依赖 AOP 门面。
 *
 * <p>基于 JDK 动态代理（{@code java.lang.reflect.Proxy}）提供 {@code Aspect} 切面
 * 与 {@code ProxyUtil} 代理工厂，方法级匹配，覆盖接口代理场景。</p>
 */
module sure.aop {
	requires transitive sure.core;

	exports com.sure.tool.aop;
}
