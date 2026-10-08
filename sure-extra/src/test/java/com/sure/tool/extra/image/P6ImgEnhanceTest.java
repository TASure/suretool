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

import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * P6（v1.7.0）：图像增强测试（格式写出 / 旋转 / 灰度 / 圆角）。
 */
public class P6ImgEnhanceTest {

	private static BufferedImage solid(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, Color.RED.getRGB());
			}
		}
		return image;
	}

	@Test
	public void writeRoundTrip() throws Exception {
		BufferedImage image = solid(64, 48);
		java.io.File tmp = java.io.File.createTempFile("sure-img-", ".png");
		try {
			ImgUtil.write(image, tmp);
			Assert.assertTrue(tmp.length() > 0);
			BufferedImage back = ImgUtil.read(tmp);
			Assert.assertEquals(64, back.getWidth());
			Assert.assertEquals(48, back.getHeight());
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void writeJpgFormat() throws Exception {
		java.io.File tmp = java.io.File.createTempFile("sure-img-", ".jpg");
		try {
			ImgUtil.write(solid(32, 32), tmp, "jpg");
			Assert.assertTrue(tmp.length() > 0);
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void rotate90SwapsDimensions() {
		BufferedImage rotated = ImgUtil.rotate(solid(100, 60), 90);
		Assert.assertEquals(60, rotated.getWidth());
		Assert.assertEquals(100, rotated.getHeight());
	}

	@Test
	public void rotateNullSafe() {
		Assert.assertNull(ImgUtil.rotate(null, 45));
	}

	@Test
	public void grayProducesGrayImage() {
		BufferedImage gray = ImgUtil.gray(solid(40, 40));
		Assert.assertEquals(BufferedImage.TYPE_BYTE_GRAY, gray.getType());
		// 纯红转灰度后既非纯黑也非纯白（约 0.299 亮度系数）
		int grayRgb = gray.getRGB(10, 10);
		int red = grayRgb & 0xFF;
		Assert.assertTrue("灰度值越界: " + red, red > 0 && red < 255);
	}

	@Test
	public void roundKeepsSizeWithTransparentCorners() {
		BufferedImage rounded = ImgUtil.round(solid(50, 50), 10);
		Assert.assertEquals(50, rounded.getWidth());
		Assert.assertEquals(BufferedImage.TYPE_INT_ARGB, rounded.getType());
		// 左上角应透明（alpha=0）
		Assert.assertEquals(0, (rounded.getRGB(0, 0) >>> 24) & 0xFF);
		// 中心不透明
		Assert.assertEquals(255, (rounded.getRGB(25, 25) >>> 24) & 0xFF);
	}
}
