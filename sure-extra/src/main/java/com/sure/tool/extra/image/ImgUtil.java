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
package com.sure.tool.extra.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 图像增强工具（基于 JDK AWT，零第三方依赖）。
 * <p>
 * 提供 core 模块 {@code ImageUtil} 未覆盖的能力：文字/图片水印、多图拼接、
 * 等比缩略、图像类型转换。
 *
 * @author suretool
 * @since 1.1.0
 */
public class ImgUtil {

	private ImgUtil() {
	}

	/**
	 * 添加文字水印（默认左上角、半透明）。
	 *
	 * @param source 原图
	 * @param text   水印文字
	 * @return 带水印的图
	 */
	public static BufferedImage watermarkText(BufferedImage source, String text) {
		return watermarkText(source, text, new Font(Font.SANS_SERIF, Font.BOLD, 20),
				Color.WHITE, 0.5f);
	}

	/**
	 * 添加文字水印。
	 *
	 * @param source    原图
	 * @param text      水印文字
	 * @param font      字体
	 * @param color     颜色
	 * @param alpha     透明度（0~1）
	 * @return 带水印的图
	 */
	public static BufferedImage watermarkText(BufferedImage source, String text,
			Font font, Color color, float alpha) {
		if (source == null || text == null || text.isEmpty()) {
			return source;
		}
		BufferedImage target = copy(source);
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampAlpha(alpha)));
			g.setColor(color);
			g.setFont(font);
			g.drawString(text, 10, font.getSize() + 10);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 添加图片水印（默认右下角、半透明）。
	 *
	 * @param source    原图
	 * @param watermark 水印图
	 * @return 带水印的图
	 */
	public static BufferedImage watermarkImage(BufferedImage source, BufferedImage watermark) {
		return watermarkImage(source, watermark, 0.5f);
	}

	/**
	 * 添加图片水印（右下角）。
	 *
	 * @param source    原图
	 * @param watermark 水印图
	 * @param alpha     透明度（0~1）
	 * @return 带水印的图
	 */
	public static BufferedImage watermarkImage(BufferedImage source, BufferedImage watermark,
			float alpha) {
		if (source == null || watermark == null) {
			return source;
		}
		int x = source.getWidth() - watermark.getWidth() - 10;
		int y = source.getHeight() - watermark.getHeight() - 10;
		return watermarkImageAt(source, watermark, Math.max(0, x), Math.max(0, y), alpha);
	}

	/**
	 * 在指定位置添加图片水印。
	 *
	 * @param source    原图
	 * @param watermark 水印图
	 * @param x         横坐标
	 * @param y         纵坐标
	 * @param alpha     透明度（0~1）
	 * @return 带水印的图
	 */
	public static BufferedImage watermarkImageAt(BufferedImage source, BufferedImage watermark,
			int x, int y, float alpha) {
		if (source == null || watermark == null) {
			return source;
		}
		BufferedImage target = copy(source);
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampAlpha(alpha)));
			g.drawImage(watermark, x, y, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 横向拼接多图（等宽等高，白底留 4 像素间距）。
	 *
	 * @param images 图片列表
	 * @return 拼接结果
	 */
	public static BufferedImage mergeHorizontal(List<BufferedImage> images) {
		if (images == null || images.isEmpty()) {
			return null;
		}
		int gap = 4;
		int height = images.stream().mapToInt(BufferedImage::getHeight).max().orElse(1);
		int width = images.stream().mapToInt(BufferedImage::getWidth).sum() + gap * (images.size() + 1);
		BufferedImage merged = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = merged.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			int x = gap;
			for (BufferedImage image : images) {
				g.drawImage(image, x, 0, null);
				x += image.getWidth() + gap;
			}
		} finally {
			g.dispose();
		}
		return merged;
	}

	/**
	 * 纵向拼接多图。
	 *
	 * @param images 图片列表
	 * @return 拼接结果
	 */
	public static BufferedImage mergeVertical(List<BufferedImage> images) {
		if (images == null || images.isEmpty()) {
			return null;
		}
		int gap = 4;
		int width = images.stream().mapToInt(BufferedImage::getWidth).max().orElse(1);
		int height = images.stream().mapToInt(BufferedImage::getHeight).sum() + gap * (images.size() + 1);
		BufferedImage merged = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = merged.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			int y = gap;
			for (BufferedImage image : images) {
				g.drawImage(image, 0, y, null);
				y += image.getHeight() + gap;
			}
		} finally {
			g.dispose();
		}
		return merged;
	}

	/**
	 * 等比缩略（限制最大边长，保持宽高比）。
	 *
	 * @param source     原图
	 * @param maxWidth   最大宽
	 * @param maxHeight  最大高
	 * @return 缩略图
	 */
	public static BufferedImage thumbnail(BufferedImage source, int maxWidth, int maxHeight) {
		if (source == null) {
			return null;
		}
		int width = source.getWidth();
		int height = source.getHeight();
		if (width <= maxWidth && height <= maxHeight) {
			return copy(source);
		}
		double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
		int targetWidth = Math.max(1, (int) (width * ratio));
		int targetHeight = Math.max(1, (int) (height * ratio));
		BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 读取图像（支持 PNG/JPG/GIF/BMP 等 {@code ImageIO} 支持的格式）。
	 *
	 * @param file 图像文件
	 * @return 图像，读取失败返回 {@code null}
	 */
	public static BufferedImage read(File file) {
		if (file == null || !file.isFile()) {
			return null;
		}
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * 从输入流读取图像。
	 *
	 * @param in 输入流
	 * @return 图像，读取失败返回 {@code null}
	 */
	public static BufferedImage read(InputStream in) {
		if (in == null) {
			return null;
		}
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * 从字节数组读取图像。
	 *
	 * @param bytes 图像字节
	 * @return 图像，读取失败返回 {@code null}
	 */
	public static BufferedImage read(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return read(new java.io.ByteArrayInputStream(bytes));
	}

	/**
	 * 缩放图像到指定尺寸（BILINEAR 插值）。
	 *
	 * @param source 原图
	 * @param width  目标宽（<=0 时保持原宽）
	 * @param height 目标高（<=0 时保持原高）
	 * @return 缩放结果
	 */
	public static BufferedImage scale(BufferedImage source, int width, int height) {
		if (source == null) {
			return null;
		}
		int w = width <= 0 ? source.getWidth() : width;
		int h = height <= 0 ? source.getHeight() : height;
		BufferedImage target = new BufferedImage(w, h, source.getType() == BufferedImage.TYPE_INT_ARGB
				? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		Graphics2D g = target.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(source, 0, 0, w, h, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	/**
	 * 裁剪图像（越界区域留白）。
	 *
	 * @param source 原图
	 * @param x      起始横坐标
	 * @param y      起始纵坐标
	 * @param width  裁剪宽
	 * @param height 裁剪高
	 * @return 裁剪结果
	 */
	public static BufferedImage crop(BufferedImage source, int x, int y, int width, int height) {
		if (source == null || width <= 0 || height <= 0) {
			return null;
		}
		BufferedImage target = new BufferedImage(width, height, source.getType() == BufferedImage.TYPE_INT_ARGB
				? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		Graphics2D g = target.createGraphics();
		try {
			g.drawImage(source, -x, -y, null);
		} finally {
			g.dispose();
		}
		return target;
	}

	private static BufferedImage copy(BufferedImage source) {
		BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(),
				source.getType() == BufferedImage.TYPE_INT_ARGB ? BufferedImage.TYPE_INT_ARGB
						: BufferedImage.TYPE_INT_RGB);
		Graphics2D g = copy.createGraphics();
		try {
			g.drawImage(source, 0, 0, null);
		} finally {
			g.dispose();
		}
		return copy;
	}

	private static float clampAlpha(float alpha) {
		return Math.max(0f, Math.min(1f, alpha));
	}
}
