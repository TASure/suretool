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

package com.sure.tool.io;

import java.nio.file.Path;

/**
 * 目录事件路径过滤器（后缀/前缀/正则均可自定义实现）。
 */
@FunctionalInterface
public interface PathFilter {

	/**
	 * 是否接受该事件路径。
	 *
	 * @param relativePath 相对监控根目录的路径
	 * @return 是否接受
	 */
	boolean accept(Path relativePath);
}
