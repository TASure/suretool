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
 * sure-template：模板引擎门面。
 *
 * <p>提供与具体引擎解耦的 {@link com.sure.tool.template.Template} 接口，
 * 内置零依赖的 {@code SimpleTemplate}（${} 占位符、转义、Bean 渲染）。</p>
 */
module sure.template {
	requires transitive sure.core;

	exports com.sure.tool.template;
}
