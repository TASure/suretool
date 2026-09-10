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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;

import com.sure.tool.thread.LockUtil;

/**
 * LockUtil 测试：全局锁缓存、函数式读写与异常释放。
 */
public class LockUtilTest {

	@Test
	public void testCreateReadWriteLockCached() {
		ReentrantReadWriteLock lock1 = LockUtil.createReadWriteLock("cache-lock");
		ReentrantReadWriteLock lock2 = LockUtil.createReadWriteLock("cache-lock");
		assertSame(lock1, lock2);
		assertNotSame(lock1, LockUtil.createReadWriteLock("other-lock"));
	}

	@Test
	public void testGetLock() {
		assertSame(LockUtil.getLock("mutex"), LockUtil.getLock("mutex"));
	}

	@Test
	public void testReadWriteSupplier() {
		ReentrantReadWriteLock lock = LockUtil.createReadWriteLock("rw");
		assertEquals(Integer.valueOf(1), LockUtil.read(lock, () -> 1));
		assertEquals("ok", LockUtil.write(lock, () -> "ok"));
	}

	@Test
	public void testRunReadWrite() {
		ReentrantReadWriteLock lock = LockUtil.createReadWriteLock("run");
		AtomicInteger counter = new AtomicInteger();
		LockUtil.runRead(lock, counter::incrementAndGet);
		LockUtil.runWrite(lock, counter::incrementAndGet);
		assertEquals(2, counter.get());
	}

	@Test
	public void testLockReleasedAfterException() {
		ReentrantReadWriteLock lock = LockUtil.createReadWriteLock("exception");
		try {
			LockUtil.write(lock, () -> {
				throw new IllegalStateException("boom");
			});
			fail("应抛出异常");
		} catch (IllegalStateException expected) {
			// 预期
		}
		// 锁已释放，可再次获取写锁不死锁
		assertEquals("recovered", LockUtil.write(lock, () -> "recovered"));
	}

	@Test
	public void testReadReleasedAfterException() {
		ReentrantReadWriteLock lock = LockUtil.createReadWriteLock("read-exception");
		try {
			LockUtil.runRead(lock, () -> {
				throw new IllegalStateException("boom");
			});
			fail("应抛出异常");
		} catch (IllegalStateException expected) {
			// 预期
		}
		LockUtil.runRead(lock, () -> {
			// 不抛异常即代表读锁已释放
		});
	}
}
