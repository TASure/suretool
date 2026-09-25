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
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * ImgUtil 图像工具测试（read / scale / crop / watermark / merge / thumbnail）。
 */
public class P5ImgUtilTest {

	private static BufferedImage solidImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				img.setRGB(x, y, new Color(x % 256, y % 256, 128).getRGB());
			}
		}
		return img;
	}

	@Test
	public void readFromBytesAndFile() throws Exception {
		BufferedImage src = solidImage(20, 15);
		byte[] png = imageBytes(src);
		Assert.assertNotNull(png);

		BufferedImage fromBytes = ImgUtil.read(png);
		Assert.assertNotNull(fromBytes);
		Assert.assertEquals(20, fromBytes.getWidth());
		Assert.assertEquals(15, fromBytes.getHeight());

		File tmp = File.createTempFile("img-util-", ".png");
		try {
			Files.write(tmp.toPath(), png);
			BufferedImage fromFile = ImgUtil.read(tmp);
			Assert.assertNotNull(fromFile);
			Assert.assertEquals(20, fromFile.getWidth());
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void readNullReturnsNull() {
		Assert.assertNull(ImgUtil.read((byte[]) null));
		Assert.assertNull(ImgUtil.read(new byte[0]));
		Assert.assertNull(ImgUtil.read((File) null));
		Assert.assertNull(ImgUtil.read((ByteArrayInputStream) null));
		Assert.assertNull(ImgUtil.read(new File("/no/such/file.png")));
	}

	@Test
	public void scaleResizesAndKeepsChannel() {
		BufferedImage src = solidImage(40, 30);
		BufferedImage scaled = ImgUtil.scale(src, 20, 15);
		Assert.assertEquals(20, scaled.getWidth());
		Assert.assertEquals(15, scaled.getHeight());

		BufferedImage scaledW = ImgUtil.scale(src, 0, 15);
		Assert.assertEquals(40, scaledW.getWidth());
		Assert.assertEquals(15, scaledW.getHeight());

		Assert.assertNull(ImgUtil.scale(null, 10, 10));
	}

	@Test
	public void cropRegion() {
		BufferedImage src = solidImage(50, 40);
		BufferedImage cropped = ImgUtil.crop(src, 10, 5, 25, 20);
		Assert.assertEquals(25, cropped.getWidth());
		Assert.assertEquals(20, cropped.getHeight());

		Assert.assertNull(ImgUtil.crop(src, 0, 0, 0, 10));
		Assert.assertNull(ImgUtil.crop(null, 0, 0, 10, 10));
	}

	@Test
	public void watermarkTextAndImage() {
		BufferedImage src = solidImage(60, 40);
		BufferedImage wm = ImgUtil.watermarkText(src, "suretool");
		Assert.assertNotNull(wm);
		Assert.assertEquals(src.getWidth(), wm.getWidth());

		BufferedImage wm2 = ImgUtil.watermarkText(src, "x", new Font(Font.SANS_SERIF, Font.BOLD, 18),
				Color.RED, 0.8f);
		Assert.assertNotNull(wm2);

		// 空文本/空图返回原图
		Assert.assertSame(src, ImgUtil.watermarkText(src, ""));
		Assert.assertNull(ImgUtil.watermarkText(null, "t"));

		BufferedImage mark = solidImage(8, 8);
		BufferedImage wi = ImgUtil.watermarkImage(src, mark);
		Assert.assertNotNull(wi);
		BufferedImage wiAt = ImgUtil.watermarkImageAt(src, mark, 5, 5, 0.3f);
		Assert.assertNotNull(wiAt);
		Assert.assertSame(src, ImgUtil.watermarkImage(src, null));
		Assert.assertSame(src, ImgUtil.watermarkImageAt(src, null, 0, 0, 0.5f));
	}

	@Test
	public void mergeAndThumbnail() {
		List<BufferedImage> imgs = new ArrayList<>();
		imgs.add(solidImage(30, 20));
		imgs.add(solidImage(20, 25));
		BufferedImage h = ImgUtil.mergeHorizontal(imgs);
		Assert.assertNotNull(h);
		Assert.assertEquals(30 + 20 + 4 * 3, h.getWidth());
		Assert.assertEquals(25, h.getHeight());

		BufferedImage v = ImgUtil.mergeVertical(imgs);
		Assert.assertNotNull(v);
		Assert.assertEquals(30, v.getWidth());
		Assert.assertEquals(20 + 25 + 4 * 3, v.getHeight());

		Assert.assertNull(ImgUtil.mergeHorizontal(null));
		Assert.assertNull(ImgUtil.mergeHorizontal(new ArrayList<>()));

		BufferedImage src = solidImage(100, 50);
		BufferedImage t = ImgUtil.thumbnail(src, 40, 40);
		Assert.assertEquals(40, t.getWidth());
		Assert.assertEquals(20, t.getHeight());

		// 原图小于目标时返回拷贝
		BufferedImage t2 = ImgUtil.thumbnail(solidImage(10, 10), 100, 100);
		Assert.assertEquals(10, t2.getWidth());
		Assert.assertNull(ImgUtil.thumbnail(null, 10, 10));
	}

	private static byte[] imageBytes(BufferedImage img) throws Exception {
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}
}
