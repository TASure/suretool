package com.sure.tool.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * ThreadUtil / ExecutorBuilder / SyncFinisher 测试。
 */
public class ThreadUtilTest {

	@Test
	public void testExecAsyncCallable() throws Exception {
		Future<String> future = ThreadUtil.execAsync(() -> "done");
		assertEquals("done", future.get(5, TimeUnit.SECONDS));
	}

	@Test
	public void testExecAsyncRunnable() throws Exception {
		AtomicInteger counter = new AtomicInteger();
		Future<?> future = ThreadUtil.execAsync(counter::incrementAndGet);
		future.get(5, TimeUnit.SECONDS);
		assertEquals(1, counter.get());
	}

	@Test
	public void testSleep() {
		long start = System.currentTimeMillis();
		ThreadUtil.sleep(50);
		assertTrue(System.currentTimeMillis() - start >= 50);
	}

	@Test
	public void testNewThread() {
		Thread thread = ThreadUtil.newThread(() -> {
		}, "test-thread");
		assertEquals("test-thread", thread.getName());
		assertFalse(thread.isDaemon());

		Thread daemon = ThreadUtil.newThread(() -> {
		}, "daemon-thread", true);
		assertTrue(daemon.isDaemon());
	}

	@Test
	public void testGetProcessorCount() {
		assertTrue(ThreadUtil.getProcessorCount() >= 1);
	}

	@Test
	public void testNamedThreadFactory() {
		ThreadUtil.NamedThreadFactory factory = new ThreadUtil.NamedThreadFactory("test-", false);
		Thread thread1 = factory.newThread(() -> {
		});
		Thread thread2 = factory.newThread(() -> {
		});
		assertTrue(thread1.getName().startsWith("test-"));
		assertFalse(thread1.getName().equals(thread2.getName()));
	}

	@Test
	public void testExecutorBuilder() throws Exception {
		ExecutorService pool = ExecutorBuilder.create()
				.setCorePoolSize(2)
				.setMaxPoolSize(4)
				.setThreadNamePrefix("job-")
				.setQueueCapacity(100)
				.build();

		List<Future<Integer>> futures = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			final int value = i;
			futures.add(pool.submit(() -> value * 2));
		}
		for (int i = 0; i < 6; i++) {
			AssertEquals(Integer.valueOf(i * 2), futures.get(i).get(5, TimeUnit.SECONDS));
		}
		ThreadUtil.shutdownQuietly(pool);
		assertTrue(pool.isShutdown());
	}

	@Test
	public void testExecutorBuilderUnbounded() {
		ExecutorService pool = ExecutorBuilder.create().setQueueCapacity(0).build();
		assertNotNull(pool);
		ThreadUtil.shutdownQuietly(pool);
	}

	@Test
	public void testSyncFinisher() throws Exception {
		AtomicInteger counter = new AtomicInteger();
		new SyncFinisher(2)
				.repeat(4, counter::incrementAndGet)
				.sync()
				.stop();
		assertEquals(4, counter.get());
	}

	@Test
	public void testSyncFinisherException() {
		try {
			new SyncFinisher(1)
					.addWorker(() -> {
						throw new IllegalStateException("boom");
					})
					.sync();
			fail("应抛出 RuntimeException");
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		} catch (InterruptedException e) {
			fail("不应中断");
		}
	}
}
