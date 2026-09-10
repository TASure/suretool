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
package com.sure.tool.captcha;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

/**
 * 图形验证码抽象基类：随机字符生成、背景与字符绘制，干扰由子类实现，参考 Hutool 的 {@code AbstractCaptcha} 设计。
 * <p>
 * 基于 JDK AWT 绘制，兼容无头（headless）服务器环境。
 *
 * @author suretool
 * @since 0.1.0
 */
public abstract class AbstractCaptcha implements Captcha {

	/**
	 * 字符池：剔除易混淆的 0/O/1/l/I。
	 */
	protected static final String BASE_CHAR = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";

	/**
	 * 无头模式兼容。
	 */
	static {
		System.setProperty("java.awt.headless", "true");
	}

	private final int width;
	private final int height;
	private final int codeCount;
	private final int interfereCount;
	private final String code;
	private byte[] imageBytes;

	/**
	 * 创建验证码。
	 *
	 * @param width          图片宽度
	 * @param height         图片高度
	 * @param codeCount      字符个数
	 * @param interfereCount 干扰数量
	 */
	protected AbstractCaptcha(int width, int height, int codeCount, int interfereCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
		this.interfereCount = interfereCount;
		this.code = randomCode(codeCount);
		this.imageBytes = createImageBytes();
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public boolean verify(String userInputCode) {
		if (userInputCode == null) {
			return false;
		}
		return code.equalsIgnoreCase(userInputCode.trim());
	}

	@Override
	public void write(OutputStream out) throws IOException {
		out.write(getImageBytes());
		out.flush();
	}

	@Override
	public byte[] getImageBytes() {
		return imageBytes.clone();
	}

	@Override
	public String getImageBase64() {
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
	}

	/**
	 * 随机字符个数。
	 *
	 * @param count 个数
	 * @return 随机字符串
	 */
	protected String randomCode(int count) {
		StringBuilder sb = new StringBuilder(count);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < count; i++) {
			sb.append(BASE_CHAR.charAt(random.nextInt(BASE_CHAR.length())));
		}
		return sb.toString();
	}

	/**
	 * 创建图片字节（PNG）。
	 *
	 * @return 图片字节
	 */
	private byte[] createImageBytes() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(randomColor(200, 255));
			g.fillRect(0, 0, width, height);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			applyShear(g);
			drawChars(g);
			drawInterfere(g);
		} finally {
			g.dispose();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", out);
		} catch (IOException e) {
			throw new IllegalStateException("验证码图片生成失败", e);
		}
		return out.toByteArray();
	}

	/**
	 * 字符绘制：逐个随机位置、随机旋转、随机颜色。
	 *
	 * @param g 画笔
	 */
	private void drawChars(Graphics2D g) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int padding = Math.max(4, width / 20);
		int step = (width - padding * 2) / codeCount;
		Font font = new Font("SansSerif", Font.BOLD, Math.max(12, height * 2 / 3));
		g.setFont(font);
		for (int i = 0; i < code.length(); i++) {
			g.setColor(randomColor(20, 120));
			double angle = random.nextDouble(-0.35, 0.35);
			int x = padding + i * step + random.nextInt(-step / 4, step / 4);
			int y = height / 2 + random.nextInt(-height / 5, height / 5);
			g.rotate(angle, x, y);
			g.drawString(String.valueOf(code.charAt(i)), x, y);
			g.rotate(-angle, x, y);
		}
	}

	/**
	 * 干扰绘制，由子类实现。
	 *
	 * @param g 画笔
	 */
	protected abstract void drawInterfere(Graphics2D g);

	/**
	 * 错切变换钩子，默认不变换，{@link ShearCaptcha} 覆盖。
	 *
	 * @param g 画笔
	 */
	protected void applyShear(Graphics2D g) {
		// 默认无变换
	}

	/**
	 * 随机颜色。
	 *
	 * @param from 下限（含）
	 * @param to   上限（不含）
	 * @return 颜色
	 */
	protected Color randomColor(int from, int to) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return new Color(random.nextInt(from, to), random.nextInt(from, to), random.nextInt(from, to));
	}

	/**
	 * 随机干扰线段。
	 *
	 * @param g 画笔
	 */
	protected void drawRandomLines(Graphics2D g) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < interfereCount; i++) {
			g.setColor(randomColor(100, 200));
			g.setStroke(new BasicStroke((float) random.nextDouble(0.5, 2.5)));
			double x1 = random.nextDouble(width);
			double y1 = random.nextDouble(height);
			double x2 = random.nextDouble(width);
			double y2 = random.nextDouble(height);
			g.draw(new Line2D.Double(x1, y1, x2, y2));
		}
	}

	/**
	 * 随机干扰圆圈。
	 *
	 * @param g 画笔
	 */
	protected void drawRandomCircles(Graphics2D g) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < interfereCount; i++) {
			g.setColor(randomColor(100, 200));
			g.setStroke(new BasicStroke((float) random.nextDouble(0.5, 2.0)));
			int r = random.nextInt(Math.max(2, width / 8), Math.max(3, width / 3));
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			g.drawOval(x - r, y - r, r * 2, r * 2);
		}
	}

	/**
	 * 干扰数量。
	 *
	 * @return 干扰数量
	 */
	protected int getInterfereCount() {
		return interfereCount;
	}

	/**
	 * 图片高度。
	 *
	 * @return 高度
	 */
	protected int getHeight() {
		return height;
	}

	/**
	 * 图片宽度。
	 *
	 * @return 宽度
	 */
	protected int getWidth() {
		return width;
	}
}
