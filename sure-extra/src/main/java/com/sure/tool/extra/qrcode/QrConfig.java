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
package com.sure.tool.extra.qrcode;

import java.awt.image.BufferedImage;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码生成配置。
 * <p>
 * 支持尺寸、纠错级别、边距、前景/背景色与居中 Logo 图。所有 setter 均为链式调用，
 * 可直接内联构建：{@code new QrConfig().setSize(300).setLogo(logo)}。
 *
 * @author suretool
 * @since 1.7.0
 */
public class QrConfig {

	/** 默认边长（像素） */
	public static final int DEFAULT_SIZE = 256;

	private int size = DEFAULT_SIZE;
	private ErrorCorrectionLevel errorCorrection = ErrorCorrectionLevel.M;
	private int margin = 1;
	private int foreColor = 0xFF000000;
	private int backColor = 0xFFFFFFFF;
	private BufferedImage logo;

	/**
	 * 获取边长。
	 *
	 * @return 边长像素
	 */
	public int getSize() {
		return size;
	}

	/**
	 * 设置边长。
	 *
	 * @param size 边长像素（须为正数）
	 * @return this
	 */
	public QrConfig setSize(int size) {
		this.size = size;
		return this;
	}

	/**
	 * 获取纠错级别。
	 *
	 * @return 纠错级别
	 */
	public ErrorCorrectionLevel getErrorCorrection() {
		return errorCorrection;
	}

	/**
	 * 设置纠错级别。
	 *
	 * @param errorCorrection 纠错级别（L / M / Q / H）
	 * @return this
	 */
	public QrConfig setErrorCorrection(ErrorCorrectionLevel errorCorrection) {
		this.errorCorrection = errorCorrection;
		return this;
	}

	/**
	 * 获取留白边距。
	 *
	 * @return 边距（模块数）
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置留白边距。
	 *
	 * @param margin 边距（模块数，0~4 常见）
	 * @return this
	 */
	public QrConfig setMargin(int margin) {
		this.margin = margin;
		return this;
	}

	/**
	 * 获取前景色。
	 *
	 * @return ARGB 颜色值
	 */
	public int getForeColor() {
		return foreColor;
	}

	/**
	 * 设置前景色（模块颜色）。
	 *
	 * @param foreColor ARGB 颜色值，如 {@code 0xFF000000}
	 * @return this
	 */
	public QrConfig setForeColor(int foreColor) {
		this.foreColor = foreColor;
		return this;
	}

	/**
	 * 获取背景色。
	 *
	 * @return ARGB 颜色值
	 */
	public int getBackColor() {
		return backColor;
	}

	/**
	 * 设置背景色。
	 *
	 * @param backColor ARGB 颜色值，如 {@code 0xFFFFFFFF}
	 * @return this
	 */
	public QrConfig setBackColor(int backColor) {
		this.backColor = backColor;
		return this;
	}

	/**
	 * 获取居中 Logo。
	 *
	 * @return Logo 图，可为 {@code null}
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
	public BufferedImage getLogo() {
		return logo;
	}

	/**
	 * 设置居中 Logo（绘制在二维码中心，自动缩放至边长 20%）。
	 *
	 * @param logo Logo 图，可为 {@code null}（不绘制）
	 * @return this
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
	public QrConfig setLogo(BufferedImage logo) {
		this.logo = logo;
		return this;
	}
}
