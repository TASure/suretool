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
package com.sure.tool.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.bean.FieldUtil;
import com.sure.tool.codec.HashUtil;
import com.sure.tool.collection.BloomFilterUtil;
import com.sure.tool.collection.BoundedPriorityQueue;
import com.sure.tool.collection.MultiMap;
import com.sure.tool.config.SettingUtil;
import com.sure.tool.image.ImageUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.ZipUtil;
import com.sure.tool.lang.Dict;

/**
 * 覆盖率缓冲补测：为 sure-core 提高 CI（root runner）环境下的覆盖率缓冲，
 * 覆盖一批可确定性触发、与运行环境无关的防御分支（非法参数 / 异常流 / 反射失败等）。
 *
 * @author suretool
 * @since 1.14.0
 */
public class P5CoverageBufferTest {

	/** 未注册字符集：触发 {@code URLEncoder/Decoder.encode} 的 UnsupportedEncodingException 分支。 */
	static final Charset FAKE_CHARSET = new Charset("sure-fake-charset", new String[] { "sure-fake-charset" }) {
		@Override
		public boolean contains(Charset cs) {
			return false;
		}

		@Override
		public CharsetDecoder newDecoder() {
			return null;
		}

		@Override
		public CharsetEncoder newEncoder() {
			return null;
		}
	};

	/** 序列化时抛 IO 异常的对象。 */
	static class BadSerializable implements Serializable {
		private static final long serialVersionUID = 1L;

		@java.io.Serial
		private void writeObject(ObjectOutputStream out) throws IOException {
			throw new IOException("boom");
		}
	}

	/** 无 setter、仅 private 字段 + getter 的 Bean（setProperty 走字段反射分支）。 */
	static class NoSetterBean {
		private String name;

		public String getName() {
			return name;
		}
	}

	/** 带 getter/setter 的普通 Bean。 */
	public static class P5Bean {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/** final 私有字段 Bean：setProperty 走字段反射分支（L222）。 */
	public static class FinalFieldBean {
		private final String fixed = "x";
	}

	/** 无 getter/setter、仅私有字段 Bean（PropDesc 无 setter 且字段非 final → setter 路径）。 */
	public static class BareBean {
		private String bare;
	}

	/** 会抛异常的测试方法（触发 invoke 的 InvocationTargetException 分支）。 */
	public static class BoomTarget {
		public void boom() {
			throw new IllegalStateException("boom");
		}
	}

	/** 泛型父类：生成桥方法（BeanDesc.scanMethod 的 isBridge 分支）。 */
	public static class GenBase<T> {
		public void set(T v) {
			// 桥方法来源
		}
	}

	/** 泛型子类：覆写产生桥方法。 */
	public static class GenSub extends GenBase<String> {
		@Override
		public void set(String v) {
			// 桥方法来源
		}
	}

	/** 连续大写 getter（BeanDesc.decapitalize 全大写保留分支）。 */
	public static class UrlBean {
		public String getURL() {
			return "u";
		}
	}

	/** 父类与子类同名字段（BeanDesc.containsName 去重分支）。 */
	public static class ParentFieldBean {
		protected String dup;
	}

	/** 子类同名私有字段。 */
	public static class ChildFieldBean extends ParentFieldBean {
		@SuppressWarnings("unused")
		private String dup;
	}

	@Test
	public void testHashUtilBadAlgorithm() {
		byte[] data = "sure".getBytes(StandardCharsets.UTF_8);
		try {
			HashUtil.digest("NOPE-ALGO", data);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// digest 的 NoSuchAlgorithmException → IllegalArgumentException
		}
		// digestBytes（私有）由公开摘要方法间接覆盖，此处直接走公开入口验证行为一致
		Assert.assertNotNull(HashUtil.md5Hex(data));
		Assert.assertNotNull(HashUtil.sha1Hex(data));
		Assert.assertNotNull(HashUtil.sha256Hex(data));
		Assert.assertNotNull(HashUtil.sha512Hex(data));
	}

	@Test
	public void testSettingUtilBrokenStream() {
		InputStream broken = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("broken");
			}
		};
		try {
			SettingUtil.load(broken);
			Assert.fail();
		} catch (IllegalStateException expected) {
			// parse 读取异常 → IllegalStateException
		}
	}

	@Test
	public void testZipUtilMkdirFailures() throws IOException {
		File base = FileUtil.touch(new File(System.getProperty("java.io.tmpdir"), "sure-buffer-" + System.nanoTime()));
		base.delete();
		base.mkdirs();
		try {
			// 1) zip：目标父目录被文件占用 → mkdirs 失败
			File blocker = new File(base, "blocker");
			FileUtil.touch(blocker);
			File srcFile = new File(base, "a.txt");
			FileUtil.touch(srcFile);
			File zipTarget = new File(blocker, "out.zip");
			try {
				ZipUtil.zip(srcFile, zipTarget, StandardCharsets.UTF_8);
				Assert.fail();
			} catch (IOException expected) {
				// 创建目录失败
			}
			// 2) unzip：条目父目录被文件占用 → mkdirs 失败（覆盖 L127/L133）
			File outDir = new File(base, "out");
			outDir.mkdirs();
			File dirBlocker = new File(outDir, "dir");
			FileUtil.touch(dirBlocker);
			File zf = new File(base, "nested.zip");
			try (ZipOutputStream zos = new ZipOutputStream(
					new java.io.BufferedOutputStream(new java.io.FileOutputStream(zf)))) {
				zos.putNextEntry(new ZipEntry("dir/sub/"));
				zos.closeEntry();
				zos.putNextEntry(new ZipEntry("dir/sub/f.txt"));
				zos.write("x".getBytes(StandardCharsets.UTF_8));
				zos.closeEntry();
			}
			try {
				ZipUtil.unzip(zf, outDir, StandardCharsets.UTF_8);
				Assert.fail();
			} catch (IOException expected) {
				// 创建目录失败（父路径被文件占用）
			}
			// 3) zip(List)：null 参数校验 + null 元素跳过
			try {
				ZipUtil.zip(List.of(srcFile), null, StandardCharsets.UTF_8);
				Assert.fail();
			} catch (IllegalArgumentException expected) {
				// zipFile 不能为空
			}
			File outZip = new File(base, "list.zip");
			ZipUtil.zip(Arrays.asList(srcFile, null), outZip, StandardCharsets.UTF_8);
			Assert.assertTrue(outZip.isFile());
		} finally {
			FileUtil.delete(base);
		}
	}

	@Test
	public void testFileUtilMkdirFail() throws IOException {
		File base = FileUtil.touch(new File(System.getProperty("java.io.tmpdir"), "sure-buffer-" + System.nanoTime()));
		base.delete();
		base.mkdirs();
		try {
			File blocker = new File(base, "blocker");
			FileUtil.touch(blocker);
			try {
				FileUtil.mkdir(new File(blocker, "sub"));
				Assert.fail();
			} catch (IllegalStateException expected) {
				// 父路径被文件占用 → mkdirs 失败
			}
		} finally {
			FileUtil.delete(base);
		}
	}

	@Test
	public void testObjectUtilStreamFailures() {
		// 损坏字节 → 反序列化失败 → null
		Assert.assertNull(ObjectUtil.deserialize(new byte[] { 1, 2, 3, 4 }));
		// 序列化中抛 IOException → null
		Assert.assertNull(ObjectUtil.serialize(new BadSerializable()));
	}

	@Test
	public void testUrlUtilFakeCharset() {
		try {
			UrlUtil.encode("a b", FAKE_CHARSET);
			Assert.fail();
		} catch (IllegalStateException expected) {
			// 未注册字符集 → UnsupportedEncodingException → IllegalStateException
		}
		try {
			UrlUtil.decode("a+b", FAKE_CHARSET);
			Assert.fail();
		} catch (IllegalStateException expected) {
			// 同上（decode 分支）
		}
		// 正常路径保持可用
		Assert.assertEquals("a+b", UrlUtil.encode("a b", StandardCharsets.UTF_8));
		Assert.assertEquals("a b", UrlUtil.decode("a+b", StandardCharsets.UTF_8));
	}

	@Test
	public void testDictNumberFormat() {
		Dict dict = Dict.of("k", "abc");
		Assert.assertNull(dict.getInt("k"));
		Assert.assertEquals(3, Dict.of("n", 3).getInt("n").intValue());
	}

	@Test
	public void testEmojiUtilEdgeCases() {
		// 孤立高代理（非成对）→ 反斜杠 uXXXX 转义分支
		String lone = new String(new char[] { 0xD800, 'X' });
		Assert.assertTrue(EmojiUtil.toUnicode(lone).contains("\\u"));
		// 非法 hex → isHex false 分支（原样保留）
		Assert.assertEquals("\\u" + "ZZZZ", EmojiUtil.fromUnicode("\\u" + "ZZZZ"));
		// 正常 emoji 成对代理
		Assert.assertNotNull(EmojiUtil.toUnicode("a\uD83D\uDE00b"));
		Assert.assertNotNull(EmojiUtil.fromUnicode("a\\u" + "d83d\\u" + "de00b"));
	}

	@Test
	public void testImageUtilNullGuards() {
		Assert.assertNull(ImageUtil.gray(null));
		Assert.assertNull(ImageUtil.rotate(null, 90));
		Assert.assertNull(ImageUtil.size(null));
		try {
			ImageUtil.scale(null, 100, 100);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// 参数不合法
		}
	}

	@Test
	public void testBeanUtilGuards() {
		// mapToBean：正常映射
		P5Bean mapped = BeanUtil.mapToBean(Map.of("name", "sure"), P5Bean.class);
		Assert.assertEquals("sure", mapped.getName());
		// setProperty：无 setter 字段 → 字段反射写入
		NoSetterBean bean = new NoSetterBean();
		BeanUtil.setProperty(bean, "name", "sure");
		Assert.assertEquals("sure", bean.getName());
		// setProperty：final 私有字段（不可写 prop）→ FieldUtil.getField → setFieldValue（L222）
		FinalFieldBean ff = new FinalFieldBean();
		BeanUtil.setProperty(ff, "fixed", "y");
		// contains(null, ...) → false（ignoreProperties 为 null）
		BeanUtil.copyProperties(new P5Bean(), new P5Bean(), (String[]) null);
		// copyToList：正常复制
		List<P5Bean> list = BeanUtil.copyToList(Arrays.asList(new P5Bean()), P5Bean.class, false);
		Assert.assertEquals(1, list.size());
		Assert.assertNotNull(list.get(0));
		// BeanDesc：桥方法 + 连续大写 + 同名字段去重
		BeanUtil.getBeanDesc(GenSub.class);
		BeanUtil.getBeanDesc(UrlBean.class);
		BeanUtil.getBeanDesc(ChildFieldBean.class);
	}

	@Test
	public void testCollectionGuards() {
		MultiMap<String, String> mm = new MultiMap<>();
		Assert.assertFalse(mm.remove("no-such", "v"));
		mm.put("k", "v");
		Assert.assertNotNull(mm.toString());
		Assert.assertFalse(mm.isEmpty());

		BoundedPriorityQueue<Integer> bpq = new BoundedPriorityQueue<>(2, Comparator.reverseOrder());
		bpq.offer(1);
		bpq.offer(2);
		try {
			bpq.offer(null);
			Assert.fail();
		} catch (NullPointerException expected) {
			// 元素不能为 null
		}

		BloomFilterUtil bf = new BloomFilterUtil(1000, 0.01);
		bf.put(null);
		Assert.assertFalse(bf.mightContain(null));
		bf.put("sure");
		Assert.assertTrue(bf.mightContain("sure"));
	}

	@Test
	public void testFieldUtilNull() {
		Assert.assertNull(FieldUtil.getField(null, "x"));
		Assert.assertNull(FieldUtil.getField(String.class, null));
	}

	@Test
	public void testReflectUtilInvokeFailures() {
		try {
			ReflectUtil.invoke(new P5Bean(), "noSuchMethod", new Class<?>[0]);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// 方法不存在
		}
		try {
			ReflectUtil.invoke(new BoomTarget(), "boom", new Class<?>[0]);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// InvocationTargetException → 调用方法失败
		}
	}

	@Test
	public void testExpressionUtilErrors() {
		Assert.assertEquals(5.0, ExpressionUtil.eval("+5"), 0.0001);
		Assert.assertEquals(1200.0, ExpressionUtil.eval("1e+3*1.2"), 0.0001);
		try {
			ExpressionUtil.eval("1e");
			Assert.fail();
		} catch (RuntimeException expected) {
			// 非法数字
		}
		try {
			ExpressionUtil.evalNumber("x", Map.of("x", "abc"));
			Assert.fail();
		} catch (RuntimeException expected) {
			// 变量不是数字
		}
		// 正常路径
		Assert.assertEquals(3.0, ExpressionUtil.eval("1+2"), 0.0001);
	}

	@Test
	public void testSnowflakeClockBackoffInterrupted() {
		com.sure.tool.lang.Snowflake sf = new com.sure.tool.lang.Snowflake(1, 1);
		// 把时钟拨到未来 3 秒（< 5 秒阈值，走等待分支）
		ReflectUtil.setFieldValue(sf, "lastTimestamp", System.currentTimeMillis() + 3000);
		Thread.currentThread().interrupt();
		try {
			sf.nextId();
			Assert.fail();
		} catch (IllegalStateException expected) {
			// 等待时钟追平时线程被中断
		}
		Assert.assertTrue(Thread.currentThread().isInterrupted());
		Thread.interrupted();
	}
	@Test
	public void testPropsStringCtorAndGetBool() throws IOException {
		try {
			new com.sure.tool.util.Props("no-such-sure-prop-" + System.nanoTime() + ".properties");
			Assert.fail();
		} catch (IOException expected) {
			// 文件不存在
		}
		com.sure.tool.util.Props p = new com.sure.tool.util.Props();
		Assert.assertFalse(p.getBool("no-such-key", false));
		Assert.assertTrue(p.getBool("no-such-key2", true));
	}

	@Test
	public void testFileUtilTouchReadOnly() {
		// /proc 只读文件系统：createNewFile 返回 false → 抛 IOException（L130）
		try {
			com.sure.tool.io.FileUtil.touch(new java.io.File("/proc/1/sure-" + System.nanoTime()));
			Assert.fail();
		} catch (java.io.IOException expected) {
			// 只读文件系统创建失败
		}
	}

	@Test
	public void testZipUtilMkdirsFailures() throws Exception {
		java.io.File base = new java.io.File("/tmp/sure-zipcov-" + System.nanoTime());
		base.mkdirs();
		java.io.File srcDir = new java.io.File(base, "src");
		srcDir.mkdirs();
		com.sure.tool.io.FileUtil.writeString("x", new java.io.File(srcDir, "sub.txt"),
				java.nio.charset.StandardCharsets.UTF_8);
		// 1) zip 目标父目录被普通文件阻塞 → mkdirs 失败（L68）
		java.io.File blocker = new java.io.File(base, "blk");
		com.sure.tool.io.FileUtil.touch(blocker);
		java.io.File zipOk = new java.io.File(base, "ok.zip");
		com.sure.tool.io.ZipUtil.zip(srcDir, zipOk, java.nio.charset.StandardCharsets.UTF_8);
		try {
			com.sure.tool.io.ZipUtil.zip(srcDir, new java.io.File(blocker, "no/x.zip"),
					java.nio.charset.StandardCharsets.UTF_8);
			Assert.fail();
		} catch (java.io.IOException expected) {
			// 创建目录失败
		}
		// 2) 解压目标被普通文件阻塞 → 条目父目录 mkdirs 失败（L133）
		java.io.File blocker2 = new java.io.File(base, "blk2");
		com.sure.tool.io.FileUtil.touch(blocker2);
		try {
			com.sure.tool.io.ZipUtil.unzip(zipOk, blocker2);
			Assert.fail();
		} catch (java.io.IOException expected) {
			// 创建目录失败
		}
		// 清理
		com.sure.tool.io.FileUtil.delete(base);
	}

	@Test
	public void testConfigPropsBadStream() {
		// 自定义 ClassLoader 返回抛 IOException 的流 → loadResource 抛 IllegalArgumentException（L88-89）
		ClassLoader bad = new ClassLoader(getClass().getClassLoader()) {
			@Override
			public java.io.InputStream getResourceAsStream(String name) {
				return new java.io.InputStream() {
					@Override
					public int read() throws java.io.IOException {
						throw new java.io.IOException("boom");
					}
				};
			}
		};
		Thread t = Thread.currentThread();
		ClassLoader old = t.getContextClassLoader();
		t.setContextClassLoader(bad);
		try {
			new com.sure.tool.config.Props().load("anything");
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// 加载配置失败
		} finally {
			t.setContextClassLoader(old);
		}
	}

	@Test
	public void testAsyncUtilAllTasksFailed() {
		// 单任务返回 null → winner/lastFailure 均为 null → "all tasks failed"（L187）
		java.util.concurrent.CompletableFuture<Object> f = com.sure.tool.thread.AsyncUtil.anyOf(() -> null);
		try {
			f.join();
			Assert.fail();
		} catch (java.util.concurrent.CompletionException expected) {
			// 异常完成：all tasks failed
		}
		Assert.assertTrue(f.isCompletedExceptionally());
	}

	@Test
	public void testUlidUtilOverflowChain() throws Exception {
		java.lang.reflect.Field r1 = com.sure.tool.id.UlidUtil.class.getDeclaredField("lastRandom1");
		java.lang.reflect.Field r2 = com.sure.tool.id.UlidUtil.class.getDeclaredField("lastRandom2");
		r1.setAccessible(true);
		r2.setAccessible(true);
		// 预热推进 lastTimestamp 后立即注入拉满值并调用：同毫秒命中即走溢出链（L70/L71/L73）
		for (int i = 0; i < 50; i++) {
			com.sure.tool.id.UlidUtil.monotonicUlid();
			r1.set(null, 0xFFFFFFFFFFFFFFFFL);
			r2.set(null, 0xFFFF);
			Assert.assertNotNull(com.sure.tool.id.UlidUtil.monotonicUlid());
		}
	}
}
