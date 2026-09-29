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
package com.sure.tool.socket;

import java.net.Socket;

/**
 * Socket 连接处理器。
 *
 * <p>由 {@link SocketServer} 在虚拟线程中为每个连接调用，
 * 实现内处理完连接后应自行关闭 Socket（或依赖 {@link SocketServer} 的
 * try-with-resources 包装自动关闭）。</p>
 *
 * @since 1.4.0
 */
@FunctionalInterface
public interface SocketHandler {

	/**
	 * 处理单个连接。
	 *
	 * @param socket 已建立的连接（ServerSocket accept 所得）
	 * @throws Exception 处理异常（由服务端记录，不影响 accept 循环）
	 */
	void handle(Socket socket) throws Exception;
}
