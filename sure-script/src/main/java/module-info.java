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
 * sure-script：JSR-223 脚本门面。
 *
 * <p>基于 JDK 标准 {@code java.scripting}，提供引擎探测、编译缓存与快速求值。
 * 运行期零第三方依赖，脚本引擎由调用方通过依赖提供（如 GraalVM JS / Groovy）。</p>
 */
module sure.script {
	requires transitive sure.core;
	requires java.scripting;

	exports com.sure.tool.script;
}
