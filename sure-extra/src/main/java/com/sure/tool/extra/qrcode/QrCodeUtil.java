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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码工具（基于 ZXing）。
 * <p>
 * 支持生成 PNG / Base64 / BufferedImage，支持从图片数据解析内容。
 *
 * @author suretool
 * @since 1.1.0
 */
public class QrCodeUtil {

	private QrCodeUtil() {
	}

	/**
	 * 生成二维码 PNG 字节。
	 *
	 * @param content 内容
	 * @return PNG 字节
	 */
	public static byte[] generate(String content) {
		return generate(content, new QrConfig());
	}

	/**
	 * 生成二维码 PNG 字节。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @return PNG 字节
	 */
	public static byte[] generate(String content, int size) {
		return generate(content, new QrConfig().setSize(size));
	}

	/**
	 * 按自定义配置生成二维码 PNG 字节。
	 *
	 * @param content 内容
	 * @param config  二维码配置（尺寸 / 纠错 / 颜色 / Logo）
	 * @return PNG 字节
	 * @since 1.7.0
	 */
	public static byte[] generate(String content, QrConfig config) {
		return toBytes(toImage(content, config), "png");
	}

	/**
	 * 生成二维码图片对象。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @return BufferedImage
	 */
	public static BufferedImage generateImage(String content, int size) {
		return toImage(content, new QrConfig().setSize(size));
	}

	/**
	 * 按自定义配置生成二维码图片对象。
	 *
	 * @param content 内容
	 * @param config  二维码配置
	 * @return BufferedImage
	 * @since 1.7.0
	 */
	public static BufferedImage generateImage(String content, QrConfig config) {
		return toImage(content, config);
	}

	/**
	 * 生成二维码并返回 Base64 数据（无前缀）。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @return Base64 字符串
	 */
	public static String generateBase64(String content, int size) {
		return java.util.Base64.getEncoder().encodeToString(generate(content, size));
	}

	/**
	 * 按自定义配置生成二维码并返回 Base64 数据（无前缀）。
	 *
	 * @param content 内容
	 * @param config  二维码配置
	 * @return Base64 字符串
	 * @since 1.7.0
	 */
	public static String generateBase64(String content, QrConfig config) {
		return java.util.Base64.getEncoder().encodeToString(generate(content, config));
	}

	/**
	 * 解析二维码内容。
	 *
	 * @param data 图片字节（PNG/JPEG 等）
	 * @return 解码内容
	 * @throws IOException    图片读取失败
	 * @throws NotFoundException 未识别到二维码
	 */
	/**
	 * 生成二维码 PNG 并写入文件。
	 *
	 * @param content 内容
	 * @param file    目标文件（父目录需存在）
	 * @return 是否写入成功
	 */
	public static boolean generateFile(String content, java.io.File file) {
		return generateFile(content, new QrConfig(), file);
	}

	/**
	 * 生成二维码 PNG 并写入文件。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @param file    目标文件（父目录需存在）
	 * @return 是否写入成功
	 */
	public static boolean generateFile(String content, int size, java.io.File file) {
		return generateFile(content, new QrConfig().setSize(size), file);
	}

	/**
	 * 按自定义配置生成二维码 PNG 并写入文件。
	 *
	 * @param content 内容
	 * @param config  二维码配置
	 * @param file    目标文件（父目录需存在）
	 * @return 是否写入成功
	 * @since 1.7.0
	 */
	public static boolean generateFile(String content, QrConfig config, java.io.File file) {
		if (content == null || content.isEmpty() || file == null) {
			return false;
		}
		byte[] png = generate(content, config);
		try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
			out.write(png);
			return true;
		} catch (java.io.IOException e) {
			return false;
		}
	}

	public static String decode(byte[] data) throws IOException, NotFoundException {
		return decode(ImageIO.read(new ByteArrayInputStream(data)));
	}

	/**
	 * 解析二维码内容。
	 *
	 * @param image 图片
	 * @return 解码内容
	 * @throws NotFoundException 未识别到二维码
	 */
	public static String decode(BufferedImage image) throws NotFoundException {
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource(image)));
		Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
		hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
		Result result = new MultiFormatReader().decode(bitmap, hints);
		return result.getText();
	}

	private static BufferedImage toImage(String content, QrConfig config) {
		try {
			int size = config.getSize();
			Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.MARGIN, config.getMargin());
			hints.put(EncodeHintType.ERROR_CORRECTION, config.getErrorCorrection());
			BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,
					size, size, hints);
			MatrixToImageConfig imageConfig = new MatrixToImageConfig(config.getForeColor(),
					config.getBackColor());
			BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, imageConfig);
			if (config.getLogo() != null) {
				drawLogo(image, config.getLogo());
			}
			return image;
		} catch (Exception e) {
			throw new IllegalArgumentException("二维码生成失败: " + e.getMessage(), e);
		}
	}

	private static void drawLogo(BufferedImage qr, BufferedImage logo) {
		int target = Math.max(1, qr.getWidth() / 5);
		int x = (qr.getWidth() - target) / 2;
		int y = (qr.getHeight() - target) / 2;
		java.awt.Graphics2D g = qr.createGraphics();
		try {
			g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
					java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(logo, x, y, target, target, null);
		} finally {
			g.dispose();
		}
	}

	private static byte[] toBytes(BufferedImage image, String format) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, format, out);
		} catch (IOException e) {
			throw new IllegalStateException("二维码图片写出失败", e);
		}
		return out.toByteArray();
	}
}
