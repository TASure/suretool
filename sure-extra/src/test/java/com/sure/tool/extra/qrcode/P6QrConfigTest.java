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

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;

/**
 * P6（v1.7.0）：二维码配置生成测试（自定义纠错 / 颜色 / Logo / 解码往返）。
 */
public class P6QrConfigTest {

	@Test
	public void configChainDefault() {
		QrConfig config = new QrConfig();
		Assert.assertEquals(QrConfig.DEFAULT_SIZE, config.getSize());
		Assert.assertEquals(ErrorCorrectionLevel.M, config.getErrorCorrection());
		Assert.assertEquals(1, config.getMargin());
		QrConfig chained = new QrConfig().setSize(300).setMargin(2);
		Assert.assertEquals(300, chained.getSize());
		Assert.assertEquals(2, chained.getMargin());
		Assert.assertSame(chained, chained.setSize(400));
	}

	@Test
	public void generateWithCustomConfigDecodeRoundTrip() throws Exception {
		QrConfig config = new QrConfig()
				.setSize(320)
				.setErrorCorrection(ErrorCorrectionLevel.H)
				.setForeColor(0xFF123456)
				.setBackColor(0xFFFFFFFF)
				.setMargin(2);
		byte[] png = QrCodeUtil.generate("https://github.com/TASure/suretool", config);
		Assert.assertTrue(png.length > 0);
		String decoded = QrCodeUtil.decode(png);
		Assert.assertEquals("https://github.com/TASure/suretool", decoded);
	}

	@Test
	public void generateImageWithLogo() throws Exception {
		BufferedImage logo = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 40; y++) {
				logo.setRGB(x, y, 0xFF2266AA);
			}
		}
		QrConfig config = new QrConfig().setSize(300).setLogo(logo);
		BufferedImage image = QrCodeUtil.generateImage("logo-qr", config);
		Assert.assertEquals(300, image.getWidth());
		Assert.assertEquals(300, image.getHeight());
		// 中心区域不再是白色（Logo 已绘制为不透明色）
		int centerRgb = image.getRGB(image.getWidth() / 2, image.getHeight() / 2);
		Assert.assertNotEquals(0xFFFFFFFF, centerRgb);
		Assert.assertEquals("logo-qr", QrCodeUtil.decode(image));
	}

	@Test
	public void generateBase64AndFile() throws Exception {
		QrConfig config = new QrConfig().setSize(200).setErrorCorrection(ErrorCorrectionLevel.L);
		String b64 = QrCodeUtil.generateBase64("b64-content", config);
		Assert.assertFalse(b64.isEmpty());
		java.io.File tmp = java.io.File.createTempFile("sure-qr-", ".png");
		try {
			Assert.assertTrue(QrCodeUtil.generateFile("file-content", config, tmp));
			Assert.assertTrue(tmp.length() > 0);
		} finally {
			tmp.delete();
		}
	}
}
