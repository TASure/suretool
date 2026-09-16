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
package com.sure.tool.image;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 图像工具类：基于 JDK {@code ImageIO} 的读写、缩放、裁剪、灰度、旋转、格式转换，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class ImageUtil {

	private ImageUtil() {
	}

	/**
	 * 读取图像文件。
	 *
	 * @param path 图像文件路径
	 * @return 图像；文件不是有效图像返回 null
	 * @throws IOException IO 异常
	 */
	public static BufferedImage read(Path path) throws IOException {
		try (InputStream in = Files.newInputStream(path)) {
			return ImageIO.read(in);
		}
	}

	/**
	 * 写入图像到文件（按扩展名推断格式，如 png/jpg）。
	 *
	 * @param image  图像
	 * @param path   目标路径
	 * @param format 格式（png/jpg/bmp/gif）
	 * @throws IOException IO 异常
	 */
	public static void write(BufferedImage image, Path path, String format) throws IOException {
		try (OutputStream out = Files.newOutputStream(path)) {
			ImageIO.write(image, format, out);
		}
	}

	/**
	 * 缩放图像（保持宽高比，按目标宽度计算高度；quality 高质量插值）。
	 *
	 * @param source 源图像
	 * @param width  目标宽度
	 * @param height 目标高度
	 * @return 缩放后图像
	 */
	public static BufferedImage scale(BufferedImage source, int width, int height) {
		if (source == null || width <= 0 || height <= 0) {
			throw new IllegalArgumentException("参数不合法");
		}
		BufferedImage target = new BufferedImage(width, height, source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType());
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(source, 0, 0, width, height, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 裁剪图像。
	 *
	 * @param source 源图像
	 * @param x      起始 X
	 * @param y      起始 Y
	 * @param width  宽度
	 * @param height 高度
	 * @return 裁剪后图像
	 */
	public static BufferedImage crop(BufferedImage source, int x, int y, int width, int height) {
		if (source == null || width <= 0 || height <= 0 || x < 0 || y < 0
				|| x + width > source.getWidth() || y + height > source.getHeight()) {
			throw new IllegalArgumentException("裁剪区域超出图像范围");
		}
		return source.getSubimage(x, y, width, height);
	}

	/**
	 * 转为灰度图。
	 *
	 * @param source 源图像
	 * @return 灰度图
	 */
	public static BufferedImage gray(BufferedImage source) {
		if (source == null) {
			return null;
		}
		BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = target.createGraphics();
		try {
			g.drawImage(source, 0, 0, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 旋转图像（90 度整数倍）。
	 *
	 * @param source 源图像
	 * @param degrees 旋转角度（90 的倍数）
	 * @return 旋转后图像
	 */
	public static BufferedImage rotate(BufferedImage source, int degrees) {
		if (source == null) {
			return null;
		}
		int normalized = Math.floorMod(degrees, 360);
		if (normalized == 0) {
			return source;
		}
		boolean swap = normalized == 90 || normalized == 270;
		int w = swap ? source.getHeight() : source.getWidth();
		int h = swap ? source.getWidth() : source.getHeight();
		BufferedImage target = new BufferedImage(w, h, source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType());
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.translate(w / 2.0, h / 2.0);
			g.rotate(Math.toRadians(normalized));
			g.drawImage(source, -source.getWidth() / 2, -source.getHeight() / 2, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 图像尺寸信息（宽 x 高）。
	 *
	 * @param image 图像
	 * @return 如 {@code 1920x1080}
	 */
	public static String size(BufferedImage image) {
		if (image == null) {
			return null;
		}
		return image.getWidth() + "x" + image.getHeight();
	}

	/**
	 * 将图片编码为 Base64 字符串。
	 *
	 * @param image  图片
	 * @param format 格式（png/jpg 等）
	 * @return Base64 字符串
	 * @throws java.io.IOException IO 异常
	 */
	public static String toBase64(BufferedImage image, String format) throws java.io.IOException {
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		javax.imageio.ImageIO.write(image, format, out);
		return java.util.Base64.getEncoder().encodeToString(out.toByteArray());
	}

	/**
	 * 将图片编码为 Data URI（可直接用于 &lt;img src&gt;）。
	 *
	 * @param image  图片
	 * @param format 格式（png/jpg 等）
	 * @return {@code data:image/png;base64,...}
	 * @throws java.io.IOException IO 异常
	 */
	public static String toDataUri(BufferedImage image, String format) throws java.io.IOException {
		return "data:image/" + format + ";base64," + toBase64(image, format);
	}


}
