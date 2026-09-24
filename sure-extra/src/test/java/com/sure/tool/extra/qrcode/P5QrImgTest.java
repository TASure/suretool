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

import com.sure.tool.extra.image.ImgUtil;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * P5（v1.1.0）：二维码与图像增强测试。
 */
public class P5QrImgTest {

	@Test
	public void testQrGenerateAndDecode() throws Exception {
		byte[] png = QrCodeUtil.generate("https://github.com/TASure/suretool", 200);
		Assert.assertTrue(png.length > 0);
		Assert.assertEquals("https://github.com/TASure/suretool",
				QrCodeUtil.decode(png));
	}

	@Test
	public void testQrGenerateBase64() throws Exception {
		String base64 = QrCodeUtil.generateBase64("suretool", 128);
		byte[] raw = Base64.getDecoder().decode(base64);
		Assert.assertEquals("suretool", QrCodeUtil.decode(raw));
	}

	@Test
	public void testQrImageSize() {
		BufferedImage image = QrCodeUtil.generateImage("hello", 160);
		Assert.assertEquals(160, image.getWidth());
		Assert.assertEquals(160, image.getHeight());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testQrEmptyContent() {
		QrCodeUtil.generate("");
	}

	@Test
	public void testWatermarkText() {
		BufferedImage source = solid(60, 40, new Color(0, 0, 0));
		BufferedImage marked = ImgUtil.watermarkText(source, "SURE",
				new Font(Font.SANS_SERIF, Font.BOLD, 12), Color.WHITE, 0.8f);
		Assert.assertEquals(source.getWidth(), marked.getWidth());
		Assert.assertEquals(source.getHeight(), marked.getHeight());
		// 水印区域应出现非纯黑像素
		boolean changed = false;
		for (int x = 0; x < 40 && !changed; x++) {
			for (int y = 0; y < 20 && !changed; y++) {
				if (marked.getRGB(x, y) != new Color(0, 0, 0).getRGB()) {
					changed = true;
				}
			}
		}
		Assert.assertTrue("水印应改变像素", changed);
	}

	@Test
	public void testWatermarkTextEmptyText() {
		BufferedImage source = solid(20, 20, Color.WHITE);
		Assert.assertSame(source, ImgUtil.watermarkText(source, ""));
		Assert.assertNull(ImgUtil.watermarkText(null, "x"));
	}

	@Test
	public void testWatermarkImage() {
		BufferedImage source = solid(100, 100, Color.WHITE);
		BufferedImage mark = solid(20, 20, Color.RED);
		BufferedImage marked = ImgUtil.watermarkImage(source, mark);
		Assert.assertEquals(100, marked.getWidth());
		// 右下角区域（水印覆盖区 70~90）：半透明红混合白 -> (255,127,127)
		Assert.assertNotEquals(new Color(255, 255, 255).getRGB(), marked.getRGB(80, 80));
		Assert.assertEquals(new Color(255, 127, 127).getRGB(), marked.getRGB(80, 80));
	}

	@Test
	public void testWatermarkImageAt() {
		BufferedImage source = solid(50, 50, Color.WHITE);
		BufferedImage mark = solid(10, 10, Color.BLUE);
		BufferedImage marked = ImgUtil.watermarkImageAt(source, mark, 0, 0, 1f);
		Assert.assertEquals(new Color(0, 0, 255).getRGB(), marked.getRGB(5, 5));
		// 不透明外区域仍为白
		Assert.assertEquals(new Color(255, 255, 255).getRGB(), marked.getRGB(45, 45));
	}

	@Test
	public void testMergeHorizontal() {
		List<BufferedImage> images = new ArrayList<>();
		images.add(solid(10, 20, Color.RED));
		images.add(solid(10, 20, Color.GREEN));
		BufferedImage merged = ImgUtil.mergeHorizontal(images);
		Assert.assertEquals(10 + 10 + 4 * 3, merged.getWidth());
		Assert.assertEquals(20, merged.getHeight());
		// 左图区域（4~14）为红
		Assert.assertEquals(new Color(255, 0, 0).getRGB(), merged.getRGB(6, 10));
	}

	@Test
	public void testMergeVertical() {
		List<BufferedImage> images = new ArrayList<>();
		images.add(solid(20, 10, Color.RED));
		images.add(solid(20, 10, Color.GREEN));
		BufferedImage merged = ImgUtil.mergeVertical(images);
		Assert.assertEquals(20, merged.getWidth());
		Assert.assertEquals(10 + 10 + 4 * 3, merged.getHeight());
	}

	@Test
	public void testMergeEmpty() {
		Assert.assertNull(ImgUtil.mergeHorizontal(null));
		Assert.assertNull(ImgUtil.mergeVertical(new ArrayList<>()));
	}

	@Test
	public void testThumbnailScaleDown() {
		BufferedImage source = solid(200, 100, Color.WHITE);
		BufferedImage thumb = ImgUtil.thumbnail(source, 50, 50);
		// 等比：200x100 -> 50x25
		Assert.assertEquals(50, thumb.getWidth());
		Assert.assertEquals(25, thumb.getHeight());
	}

	@Test
	public void testThumbnailNoScale() {
		BufferedImage source = solid(30, 20, Color.WHITE);
		BufferedImage thumb = ImgUtil.thumbnail(source, 50, 50);
		Assert.assertEquals(30, thumb.getWidth());
		Assert.assertEquals(20, thumb.getHeight());
		Assert.assertNotSame(source, thumb);
	}

	@Test
	public void testThumbnailNull() {
		Assert.assertNull(ImgUtil.thumbnail(null, 10, 10));
	}

	private static BufferedImage solid(int width, int height, Color color) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, color.getRGB());
			}
		}
		return image;
	}
}
