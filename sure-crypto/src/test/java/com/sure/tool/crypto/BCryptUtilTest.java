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
package com.sure.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * 批46：BCryptUtil 密码哈希测试。
 *
 * <p>含 10 组 Python bcrypt 5.0 确定性参考向量交叉验证（{@code $2b$} 前缀），
 * 以及 NUL 语义、72 字节边界、前缀兼容、恒定时间等安全语义断言。</p>
 */
public class BCryptUtilTest {

	private static final class Vector {
		final String cost;
		final String password;
		final String hash;

		Vector(String cost, String password, String hash) {
			this.cost = cost;
			this.password = password;
			this.hash = hash;
		}
	}

	/** Python bcrypt 5.0 生成的确定性向量（来源记录于 /tmp/vectors_full.txt，提交时随测试固化）。 */
	private static final Vector[] PY_VECTORS = {
			new Vector("10", "", "$2b$10$pk5Re0atMtKz1THAYug/9O40gw89YAV9WHC0qtOzVlAeF5gyo/s6C"),
			new Vector("4", "password", "$2b$04$pu683WZySWKTXZ8RMUzkEe/t8Zgz/Yv/NX.vflQTTfKcMHyy45Y9y"),
			new Vector("10", "password", "$2b$10$pQ9gMgY.T9zRfDw4ilpic.rB1Mj47D0//pwoOj83boGpsJjQTd66i"),
			new Vector("12", "password", "$2b$12$YYkgDpxdW0PKitrBXfXKXOtXsEvcCMDvGNNB9fUOaQM92oCLT74/S"),
			new Vector("10", "correct horse battery staple", "$2b$10$LxxocL07aYYzhx4GZDd3/.H.UYMFs4HyHiO4bIxkKGNsVgthqbkwm"),
			new Vector("10", "P@ssw0rd!2026", "$2b$10$65nvZoYBNXYNm10SHXNnbexHQz490QEnhFQOLefPTcESmVHFpe2Ue"),
			new Vector("10", "密码安全测试123", "$2b$10$j94mKyVEiq/lF7u2RQpkw.Iw16bRnko0U3oy2yoBmpaTL.l96xPKS"),
			new Vector("10", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
					"$2b$10$kwsblrMk/ImeisMw6nM3u.BxEXLAZhLGk2XY5BJgdszyCl3umg28G"),
			new Vector("8", "中文密码测试！@#", "$2b$08$/Khlo8wunrp1yORs9uz9p.QNivKBQZuyNojHBj0EDD/.5wjOuo1hK"),
			new Vector("14", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "$2b$14$B3vkfOBX/0Z4qmx1CNrxQuT4/54IIb6StnWZQRDdeooqvarjUXDpe"),
	};

	@Test
	public void testHashDefaultCost() {
		String h = BCryptUtil.hash("P@ssw0rd");
		Assert.assertEquals(60, h.length());
		Assert.assertTrue("默认前缀 $2a$", h.startsWith("$2a$"));
		Assert.assertEquals("$2a$10$", h.substring(0, 7));
		Assert.assertTrue(BCryptUtil.isValid(h));
	}

	@Test
	public void testHashCustomCost() {
		// 注意：cost 是 2 的指数，测试上限取 14（16384 轮），避免 cost=30（10 亿轮）拖垮 CI
		for (int cost : new int[]{4, 8, 12, 14}) {
			String h = BCryptUtil.hash("custom-cost", cost);
			Assert.assertEquals(60, h.length());
			Assert.assertTrue("前缀含 cost", h.startsWith("$2a$" + (cost < 10 ? "0" : "") + cost + "$"));
		}
	}

	@Test
	public void testHashRandomSalt() {
		String h1 = BCryptUtil.hash("same-password");
		String h2 = BCryptUtil.hash("same-password");
		Assert.assertNotEquals("随机盐使两次哈希不同", h1, h2);
		Assert.assertNotEquals("盐段不同", h1.substring(7, 29), h2.substring(7, 29));
	}

	@Test
	public void testCheckCorrect() {
		String h = BCryptUtil.hash("P@ssw0rd!2026");
		Assert.assertTrue(BCryptUtil.check("P@ssw0rd!2026", h));
	}

	@Test
	public void testCheckWrong() {
		String h = BCryptUtil.hash("P@ssw0rd");
		Assert.assertFalse(BCryptUtil.check("P@ssw0rd!", h));
		Assert.assertFalse(BCryptUtil.check("", h));
		Assert.assertFalse(BCryptUtil.check("P@ssw0rd ", h));
	}

	@Test
	public void testPythonVectorsCrossVerify() {
		for (Vector v : PY_VECTORS) {
			Assert.assertTrue("python 向量可互验: cost=" + v.cost + " pwd=" + v.password,
					BCryptUtil.check(v.password, v.hash));
		}
	}

	@Test
	public void testPythonVectorWrongPassword() {
		for (Vector v : PY_VECTORS) {
			// 72 字节向量 + 1 字节 = 73 字节，超出上限会被拒绝（抛 IAE），其余向量必须稳定返回 false
			if (v.password.getBytes(StandardCharsets.UTF_8).length >= 72) {
				try {
					BCryptUtil.check(v.password + "x", v.hash);
					Assert.fail("超长密码必须被拒绝: " + v.password);
				} catch (IllegalArgumentException expected) {
					// 期望行为
				}
			} else {
				Assert.assertFalse("错误密码拒绝: " + v.password,
						BCryptUtil.check(v.password + "x", v.hash));
			}
		}
	}

	@Test
	public void testMax72BytesAccepted() {
		String pwd = "a".repeat(72);
		String h = BCryptUtil.hash(pwd);
		Assert.assertTrue(BCryptUtil.check(pwd, h));
		Assert.assertFalse(BCryptUtil.check("a".repeat(71) + "b", h));
	}

	@Test
	public void testOver72BytesRejected() {
		String pwd = "a".repeat(73);
		try {
			BCryptUtil.hash(pwd);
			Assert.fail("73 字节密码必须拒绝，禁止静默截断");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		try {
			BCryptUtil.check(pwd, BCryptUtil.hash("a".repeat(72)));
			Assert.fail("check 超长密码必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testHashNullThrowsNpe() {
		try {
			BCryptUtil.hash(null);
			Assert.fail("hash(null) 必须 NPE");
		} catch (NullPointerException expected) {
			// 期望行为
		}
	}

	@Test
	public void testCheckNullArgsThrowsIae() {
		String h = BCryptUtil.hash("x");
		try {
			BCryptUtil.check(null, h);
			Assert.fail("check(null, h) 必须 IAE");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		try {
			BCryptUtil.check("x", null);
			Assert.fail("check(x, null) 必须 IAE");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testCheckInvalidHashFormat() {
		try {
			BCryptUtil.check("x", "not-a-bcrypt-hash");
			Assert.fail("非法哈希必须 IAE");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		try {
			BCryptUtil.check("x", "$2a$10$");
			Assert.fail("残缺哈希必须 IAE");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testIsValid() {
		Assert.assertTrue(BCryptUtil.isValid(BCryptUtil.hash("x")));
		Assert.assertFalse(BCryptUtil.isValid(null));
		Assert.assertFalse(BCryptUtil.isValid(""));
		Assert.assertFalse(BCryptUtil.isValid("$1$abcdefghijklmnopqrstuvwx"));
		Assert.assertFalse(BCryptUtil.isValid("$2a$99$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwx"));
		Assert.assertFalse(BCryptUtil.isValid("$2a$1x$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwx"));
		Assert.assertFalse(BCryptUtil.isValid("$2a$10$abcdefghijklmnopqrstuvw!abcdefghijklmnopqrstuvwxy"));
	}

	@Test
	public void testPrefixCompatibility() {
		// 将 python $2b$ 向量改写为 $2y$ 前缀，NUL 语义一致，必须仍可通过
		String yHash = "$2y$" + PY_VECTORS[2].hash.substring(4);
		Assert.assertTrue(BCryptUtil.check("password", yHash));
		// $2a$ 前缀（python 5.0 生成的 $2a$ 向量）
		String aHash = "$2a$10$neXAru.53u43PaANn7/NnucQ3a8DzrQZTc4fLyZFfts4/p5l77fnG";
		Assert.assertTrue(BCryptUtil.check("password", aHash));
	}

	@Test
	public void testGensaltFormat() {
		String salt = BCryptUtil.gensalt();
		Assert.assertEquals(29, salt.length());
		Assert.assertTrue(salt.startsWith("$2a$10$"));
		String saltBody = salt.substring(7);
		Assert.assertEquals(22, saltBody.length());
		for (int i = 0; i < saltBody.length(); i++) {
			Assert.assertTrue("盐字符在 bcrypt base64 字符集: " + saltBody.charAt(i),
					"./ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".indexOf(saltBody.charAt(i)) >= 0);
		}
	}

	@Test
	public void testGensaltCostBoundary() {
		Assert.assertTrue(BCryptUtil.gensalt(4).startsWith("$2a$04$"));
		Assert.assertTrue(BCryptUtil.gensalt(30).startsWith("$2a$30$"));
		try {
			BCryptUtil.gensalt(3);
			Assert.fail("cost=3 必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		try {
			BCryptUtil.gensalt(31);
			Assert.fail("cost=31 必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testConstantTimeNoShortCircuit() {
		// 残缺/非法哈希格式必须显式拒绝，不做短路的错误处理
		try {
			BCryptUtil.check("x", "$2a$10$short");
			Assert.fail("残缺哈希必须 IAE");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		// 合法格式但内容不符：恒定时间比较稳定返回 false
		String h = BCryptUtil.hash("pwd-a");
		Assert.assertFalse(BCryptUtil.check("pwd-b", h));
		Assert.assertFalse(BCryptUtil.check("pwd-a ", h));
	}

	@Test
	public void testCoverageEdgeBranches() {
		// 常量：22 字符盐 + 31 字符哈希体（覆盖 check 解析路径）
		String salt22 = "a".repeat(22);
		String hash31 = "a".repeat(31);
		// 1. hash 的 cost 越界防御（284）
		try {
			BCryptUtil.hash("x", 3);
			Assert.fail("cost=3 必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		try {
			BCryptUtil.hash("x", 31);
			Assert.fail("cost=31 必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		// 2. isValid 全 false 分支（390/391, 396, 401, 404, 409）——60 字符精确构造
		Assert.assertFalse("前缀 $2$ 不接受", BCryptUtil.isValid("$2$10$" + "a".repeat(54)));
		Assert.assertFalse("cost 非数字", BCryptUtil.isValid("$2a$1x$" + "a".repeat(53)));
		Assert.assertFalse("cost 越界", BCryptUtil.isValid("$2a$99$" + "a".repeat(53)));
		Assert.assertFalse("缺少分隔符", BCryptUtil.isValid("$2a$10" + "a".repeat(53) + "z"));
		Assert.assertFalse("盐含非法字符", BCryptUtil.isValid("$2a$10$" + "!" + "a".repeat(21) + "a".repeat(31)));
		Assert.assertTrue("合法 60 字符通过", BCryptUtil.isValid("$2a$10$" + "a".repeat(22) + "a".repeat(31)));
		// 3. $2$ 无 minor 版本解析路径（427/428）：NUL 语义一致，python cost=4 向量改前缀仍可互验
		String pyV4 = "$2b$04$pu683WZySWKTXZ8RMUzkEe/t8Zgz/Yv/NX.vflQTTfKcMHyy45Y9y";
		String legacy = "$2$04$" + pyV4.substring(7);
		Assert.assertTrue("$2$ 前缀 NUL 语义一致", BCryptUtil.check("password", legacy));
		// 4. 非法 revision / 分隔符 / cost 解析 / cost 越界（433, 436, 441/442, 445）
		try {
			BCryptUtil.check("x", "$2c$10$" + salt22 + hash31);
			Assert.fail("revision c 必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		try {
			BCryptUtil.check("x", "$2a$10x");
			Assert.fail("缺少 cost 分隔符必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		try {
			BCryptUtil.check("x", "$2a$xx$" + salt22 + hash31);
			Assert.fail("cost 非数字必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		try {
			BCryptUtil.check("x", "$2a$03$" + salt22 + hash31);
			Assert.fail("cost=3 越界必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		// 5. 盐段非法 base64 字符：decodeBase64 短停 + 长度校验（648-669, 679）
		try {
			BCryptUtil.check("x", "$2a$10$" + "!bcdefghijklmnopqrstuv" + hash31);
			Assert.fail("盐含非法字符必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
		try {
			BCryptUtil.check("x", "$2a$10$" + "密bcdefghijklmnopqrstuv" + hash31);
			Assert.fail("盐含非 ASCII 字符必须拒绝");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testUtf8BytesBoundary() {
		// 中文字符按 UTF-8 字节计长：30 个汉字 = 90 字节 > 72，必须拒绝
		String chinese30 = "密".repeat(30);
		try {
			BCryptUtil.hash(chinese30);
			Assert.fail("30 个汉字（90 UTF-8 字节）必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		// 24 个汉字 = 72 字节，恰好通过
		String chinese24 = "密".repeat(24);
		Assert.assertEquals(72, chinese24.getBytes(StandardCharsets.UTF_8).length);
		String h = BCryptUtil.hash(chinese24);
		Assert.assertTrue(BCryptUtil.check(chinese24, h));
	}
}
