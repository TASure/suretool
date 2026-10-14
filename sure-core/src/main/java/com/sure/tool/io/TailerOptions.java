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

import java.nio.charset.Charset;
import java.time.Duration;

/**
 * 文件 tail 选项（builder 风格）。
 *
 * <p>默认值：轮询间隔 1 秒、编码自动探测（UTF-8/GBK）、从文件尾部开始跟踪、
 * 文件被删除/轮转后自动重开。
 */
public class TailerOptions {

	private Duration pollInterval = Duration.ofSeconds(1);
	private Charset charset;
	private int readFromStart;
	private boolean stopOnDelete;
	private Runnable onRotate;

	/**
	 * 轮询间隔（默认 1 秒）。
	 *
	 * @param pollInterval 轮询间隔
	 * @return this
	 */
	public TailerOptions pollInterval(Duration pollInterval) {
		this.pollInterval = pollInterval;
		return this;
	}

	/**
	 * 行编码（默认自动探测 UTF-8/GBK）。
	 *
	 * @param charset 字符集
	 * @return this
	 */
	public TailerOptions charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 从文件头开始读并跳过前 N 行后再跟踪；{@code lines <= 0} 表示从文件尾部开始（等同行 tail -f）。
	 *
	 * @param lines 跳过的历史行数
	 * @return this
	 */
	public TailerOptions readFromStart(int lines) {
		this.readFromStart = lines;
		return this;
	}

	/**
	 * 文件被删除时是否停止；默认 {@code false} 表示等待文件重建后自动重开。
	 *
	 * @param stopOnDelete 是否停止
	 * @return this
	 */
	public TailerOptions stopOnDelete(boolean stopOnDelete) {
		this.stopOnDelete = stopOnDelete;
		return this;
	}

	/**
	 * 检测到文件轮转（被替换/重建）后的回调。
	 *
	 * @param onRotate 轮转回调
	 * @return this
	 */
	public TailerOptions onRotate(Runnable onRotate) {
		this.onRotate = onRotate;
		return this;
	}

	/**
	 * 轮询间隔。
	 *
	 * @return 轮询间隔
	 */
	public Duration pollInterval() {
		return pollInterval;
	}

	/**
	 * 行编码。
	 *
	 * @return 字符集；{@code null} 表示自动探测
	 */
	public Charset charset() {
		return charset;
	}

	/**
	 * 跳过的历史行数。
	 *
	 * @return 跳过的行数
	 */
	public int readFromStart() {
		return readFromStart;
	}

	/**
	 * 是否在文件被删除时停止。
	 *
	 * @return 是否停止
	 */
	public boolean stopOnDelete() {
		return stopOnDelete;
	}

	/**
	 * 轮转回调。
	 *
	 * @return 回调；可为 {@code null}
	 */
	public Runnable onRotate() {
		return onRotate;
	}
}
