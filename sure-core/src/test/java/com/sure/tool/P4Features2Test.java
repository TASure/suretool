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
package com.sure.tool;

import com.sure.tool.io.GzipUtil;
import com.sure.tool.lang.SerializeUtil;
import com.sure.tool.thread.RateLimiter;
import com.sure.tool.util.EnumUtil;
import com.sure.tool.util.ExceptionUtil;
import com.sure.tool.util.RetryUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * P4（v0.2.0）第二批新增工具单元测试：GzipUtil / SerializeUtil / RateLimiter / RetryUtil / ExceptionUtil / EnumUtil。
 */
public class P4Features2Test {

	private enum Level {
		LOW, MEDIUM, HIGH
	}

	// ---------------- GzipUtil ----------------

	@Test
	public void testGzip() throws IOException {
		String text = "suretool gzip 压缩测试，重复内容".repeat(100);
		byte[] compressed = GzipUtil.gzip(text);
		Assert.assertTrue(compressed.length < text.getBytes(StandardCharsets.UTF_8).length);
		Assert.assertEquals(text, GzipUtil.ungzipString(compressed));

		byte[] data = "binary\0data".getBytes(StandardCharsets.UTF_8);
		byte[] round = GzipUtil.ungzip(GzipUtil.gzip(data));
		Assert.assertArrayEquals(data, round);

		Assert.assertNull(GzipUtil.gzip((String) null));
		Assert.assertNull(GzipUtil.ungzipString(null));
		Assert.assertArrayEquals(new byte[0], GzipUtil.gzip(new byte[0])); // 空输入原样返回
	}

	@Test
	public void testGzipFile() throws IOException {
		Path src = Files.createTempFile("sure-gzip", ".txt");
		Path gz = Files.createTempFile("sure-gzip", ".gz");
		Path out = Files.createTempFile("sure-gzip-out", ".txt");
		try {
			Files.writeString(src, "文件压缩测试内容");
			GzipUtil.gzipFile(src, gz);
			GzipUtil.ungzipFile(gz, out);
			Assert.assertEquals("文件压缩测试内容", Files.readString(out));
		} finally {
			Files.deleteIfExists(src);
			Files.deleteIfExists(gz);
			Files.deleteIfExists(out);
		}
	}

	// ---------------- SerializeUtil ----------------

	@Test
	public void testSerialize() throws IOException, ClassNotFoundException {
		Person p = new Person("张三", 30);
		byte[] data = SerializeUtil.serialize(p);
		Person copy = SerializeUtil.deserialize(data);
		Assert.assertEquals("张三", copy.getName());
		Assert.assertEquals(30, copy.getAge());
		Assert.assertNotSame(p, copy);

		Assert.assertNull(SerializeUtil.serialize(null));
		Assert.assertNull(SerializeUtil.deserialize(null));

		Path file = Files.createTempFile("sure-serial", ".bin");
		try {
			SerializeUtil.write(p, file);
			Person fromFile = SerializeUtil.read(file);
			Assert.assertEquals("张三", fromFile.getName());
		} finally {
			Files.deleteIfExists(file);
		}

		Person clone = SerializeUtil.clone(p);
		Assert.assertNotSame(p, clone);
		Assert.assertEquals("张三", clone.getName());
		Assert.assertNull(SerializeUtil.clone(null));
	}

	// ---------------- RateLimiter ----------------

	@Test
	public void testRateLimiter() {
		RateLimiter limiter = new RateLimiter(100);
		Assert.assertTrue(limiter.tryAcquire());
		Assert.assertTrue(limiter.tryAcquire(50));
		Assert.assertFalse(limiter.tryAcquire(1000)); // 超出容量
		try {
			new RateLimiter(0);
			Assert.fail("速率 0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			limiter.tryAcquire(0);
			Assert.fail("permits 0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	@Test
	public void testRateLimiterAcquire() throws InterruptedException {
		RateLimiter limiter = new RateLimiter(10);
		long start = System.nanoTime();
		limiter.acquire(10);
		long costMs = (System.nanoTime() - start) / 1_000_000;
		Assert.assertTrue("首轮应立即可用: " + costMs, costMs < 500);
		start = System.nanoTime();
		limiter.acquire(10); // 需要等待约 1 秒
		costMs = (System.nanoTime() - start) / 1_000_000;
		Assert.assertTrue("应等待补充令牌: " + costMs, costMs >= 700);
	}

	// ---------------- RetryUtil ----------------

	@Test
	public void testRetry() throws InterruptedException {
		AtomicInteger attempts = new AtomicInteger();
		boolean ok = RetryUtil.retry(() -> {
			attempts.incrementAndGet();
			return attempts.get() >= 3;
		}, 5, Duration.ofMillis(10));
		Assert.assertTrue(ok);
		Assert.assertEquals(3, attempts.get());

		// 达到最大次数仍未成功
		Assert.assertFalse(RetryUtil.retry(() -> false, 2, Duration.ofMillis(5)));

		// 异常重试
		AtomicInteger errs = new AtomicInteger();
		boolean okErr = RetryUtil.retry(() -> {
			errs.incrementAndGet();
			if (errs.get() < 2) {
				throw new IllegalStateException("boom");
			}
			return true;
		}, 3, Duration.ofMillis(5));
		Assert.assertTrue(okErr);

		// retryOnError=false 时异常立即抛出
		try {
			RetryUtil.retry(() -> {
				throw new IllegalStateException("x");
			}, 3, Duration.ofMillis(5), false);
			Assert.fail("应抛出 IllegalStateException");
		} catch (IllegalStateException expected) {
			// expected
		}
	}

	@Test
	public void testRetryUntil() throws InterruptedException {
		AtomicInteger n = new AtomicInteger();
		String result = RetryUtil.retryUntil(() -> "v" + n.incrementAndGet(), v -> v.equals("v3"), 5, Duration.ofMillis(5));
		Assert.assertEquals("v3", result);

		try {
			RetryUtil.retryUntil(() -> {
				throw new RuntimeException("x");
			}, v -> true, 2, Duration.ofMillis(5));
			Assert.fail("应抛出 IllegalStateException");
		} catch (IllegalStateException expected) {
			// expected
		}
	}

	// ---------------- ExceptionUtil ----------------

	@Test
	public void testExceptionUtil() {
		IllegalStateException root = new IllegalStateException("root");
		RuntimeException mid = new RuntimeException("mid", root);
		IOException top = new IOException("top", mid);

		Assert.assertTrue(ExceptionUtil.getStackTrace(top).contains("top"));
		Assert.assertSame(root, ExceptionUtil.getRootCause(top));
		Assert.assertSame(root, ExceptionUtil.getRootCause(mid));
		Assert.assertSame(root, ExceptionUtil.getRootCause(root));
		Assert.assertTrue(ExceptionUtil.getMessage(top).contains("IOException"));
		Assert.assertTrue(ExceptionUtil.isCausedBy(top, IllegalStateException.class));
		Assert.assertFalse(ExceptionUtil.isCausedBy(top, NumberFormatException.class));
		Assert.assertNull(ExceptionUtil.getStackTrace(null));
		Assert.assertNull(ExceptionUtil.getRootCause(null));
		Assert.assertNull(ExceptionUtil.getMessage(null));

		RuntimeException wrapped = ExceptionUtil.wrap(new IOException("e"));
		Assert.assertNotNull(wrapped);
		Assert.assertSame(mid, ExceptionUtil.wrap(mid));
	}

	// ---------------- EnumUtil ----------------

	@Test
	public void testEnumUtil() {
		Assert.assertSame(Level.HIGH, EnumUtil.fromName(Level.class, "HIGH"));
		Assert.assertSame(Level.HIGH, EnumUtil.fromName(Level.class, "high", true));
		Assert.assertNull(EnumUtil.fromName(Level.class, "NOPE"));
		Assert.assertSame(Level.MEDIUM, EnumUtil.fromOrdinal(Level.class, 1));
		Assert.assertNull(EnumUtil.fromOrdinal(Level.class, 99));
		Assert.assertSame(Level.LOW, EnumUtil.fromString(Level.class, "LOW"));
		Assert.assertTrue(EnumUtil.containsName(Level.class, "medium"));
		Assert.assertFalse(EnumUtil.containsName(Level.class, "medium", false));
		Assert.assertSame(Level.LOW, EnumUtil.valueOfIgnoreCase(Level.class, "low"));
		Assert.assertNull(EnumUtil.fromName(null, "X"));
		try {
			EnumUtil.valueOfIgnoreCase(Level.class, "NOPE");
			Assert.fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	/** 测试用可序列化 POJO。 */
	public static class Person implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name;
		private int age;

		public Person() {
		}

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
}
