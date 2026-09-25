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

	private static final int DEFAULT_SIZE = 256;
	private static final Map<EncodeHintType, Object> DEFAULT_HINTS = defaultHints();

	private QrCodeUtil() {
	}

	/**
	 * 生成二维码 PNG 字节。
	 *
	 * @param content 内容
	 * @return PNG 字节
	 */
	public static byte[] generate(String content) {
		return generate(content, DEFAULT_SIZE);
	}

	/**
	 * 生成二维码 PNG 字节。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @return PNG 字节
	 */
	public static byte[] generate(String content, int size) {
		return toBytes(toImage(content, size), "png");
	}

	/**
	 * 生成二维码图片对象。
	 *
	 * @param content 内容
	 * @param size    边长像素
	 * @return BufferedImage
	 */
	public static BufferedImage generateImage(String content, int size) {
		return toImage(content, size);
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
		return generateFile(content, DEFAULT_SIZE, file);
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
		if (content == null || content.isEmpty() || file == null) {
			return false;
		}
		byte[] png = generate(content, size);
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

	private static BufferedImage toImage(String content, int size) {
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,
					size, size, DEFAULT_HINTS);
			MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF);
			return MatrixToImageWriter.toBufferedImage(matrix, config);
		} catch (Exception e) {
			throw new IllegalArgumentException("二维码生成失败: " + e.getMessage(), e);
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

	private static Map<EncodeHintType, Object> defaultHints() {
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 1);
		hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
		return hints;
	}
}
