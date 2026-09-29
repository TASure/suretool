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
 * sure-event：轻量事件总线。
 *
 * <p>线程安全的发布/订阅：函数式注册与 {@code @EventSubscribe} 注解注册，
 * 同步投递与虚拟线程异步投递，可插拔异常处理。</p>
 */
module sure.event {
	requires transitive sure.core;

	exports com.sure.tool.event;
}
