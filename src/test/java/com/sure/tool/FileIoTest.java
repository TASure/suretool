package com.sure.tool;

import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;
import com.sure.tool.util.CharsetUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * FileUtil / IoUtil 单元测试。
 */
public class FileIoTest {

	private File tempDir;

	@Before
	public void setUp() {
		tempDir = new File(System.getProperty("java.io.tmpdir"), "suretool-test-" + System.nanoTime());
		FileUtil.mkdir(tempDir);
	}

	@After
	public void tearDown() {
		FileUtil.delete(tempDir);
	}

	@Test
	public void testWriteReadString() throws IOException {
		File file = new File(tempDir, "test.txt");
		FileUtil.writeUtf8String("你好，suretool", file);
		Assert.assertEquals("你好，suretool", FileUtil.readUtf8String(file));
		FileUtil.appendUtf8String("！", file);
		Assert.assertEquals("你好，suretool！", FileUtil.readUtf8String(file));
		Assert.assertEquals("test", FileUtil.mainName("test.txt"));
		Assert.assertEquals("txt", FileUtil.getExt(file));
	}

	@Test
	public void testWriteReadLines() throws IOException {
		File file = new File(tempDir, "lines.txt");
		FileUtil.writeUtf8Lines(Arrays.asList("a", "b", "c"), file);
		List<String> lines = FileUtil.readUtf8Lines(file);
		Assert.assertEquals(Arrays.asList("a", "b", "c"), lines);
		Assert.assertEquals(1, FileUtil.listFileNames(tempDir.getAbsolutePath()).size());
	}

	@Test
	public void testReadBytesAndCharset() throws IOException {
		File file = new File(tempDir, "gbk.txt");
		FileUtil.writeString("中文", file, CharsetUtil.GBK);
		byte[] bytes = FileUtil.readBytes(file);
		Assert.assertEquals("中文", new String(bytes, CharsetUtil.GBK));
		Assert.assertEquals("中文", FileUtil.readString(file, CharsetUtil.GBK));
	}

	@Test
	public void testCopyMoveDelete() throws IOException {
		File src = new File(tempDir, "src.txt");
		FileUtil.writeUtf8String("content", src);
		File dest = new File(tempDir, "sub/dest.txt");
		FileUtil.copy(src, dest);
		Assert.assertTrue(dest.exists());
		Assert.assertEquals("content", FileUtil.readUtf8String(dest));

		File moved = new File(tempDir, "moved.txt");
		FileUtil.move(dest, moved);
		Assert.assertFalse(dest.exists());
		Assert.assertTrue(moved.exists());
		Assert.assertTrue(FileUtil.delete(moved));
		Assert.assertFalse(moved.exists());
	}

	@Test
	public void testCopyDirectory() throws IOException {
		File dir = new File(tempDir, "dir");
		FileUtil.mkdir(dir);
		FileUtil.writeUtf8String("1", new File(dir, "a.txt"));
		FileUtil.writeUtf8String("2", new File(dir, "b.txt"));
		File dest = new File(tempDir, "dir-copy");
		FileUtil.copy(dir, dest);
		Assert.assertEquals(2, FileUtil.loopFiles(dest).size());
		Assert.assertEquals(2, FileUtil.loopFiles(dir).size());
	}

	@Test
	public void testSizeAndReadable() throws IOException {
		File file = new File(tempDir, "size.bin");
		FileUtil.writeBytes(new byte[2048], file);
		Assert.assertEquals(2048L, FileUtil.size(file));
		Assert.assertEquals("2 KB", FileUtil.readableFileSize(2048));
		Assert.assertEquals("1.5 MB", FileUtil.readableFileSize(1024L * 1536));
		Assert.assertEquals("0 B", FileUtil.readableFileSize(0));
	}

	@Test
	public void testNormalize() {
		Assert.assertEquals("a/b/c", FileUtil.normalize("a\\b\\c"));
		Assert.assertEquals("/a/b", FileUtil.normalize("/a//b"));
		Assert.assertEquals("a/b", FileUtil.normalize("a/./b/"));
		Assert.assertEquals("a", FileUtil.normalize("a/b/../"));
		Assert.assertEquals("C:/a/b", FileUtil.normalize("C:\\a\\b"));
	}

	@Test
	public void testIoCopy() throws IOException {
		String content = "hello io";
		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long copied = IoUtil.copy(in, out);
		Assert.assertEquals(content.getBytes(StandardCharsets.UTF_8).length, copied);
		Assert.assertEquals(content, out.toString("UTF-8"));

		InputStream in2 = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("abc", IoUtil.readUtf8(in2));
	}

	@Test
	public void testIoContentEquals() throws IOException {
		InputStream in1 = new ByteArrayInputStream("same".getBytes(StandardCharsets.UTF_8));
		InputStream in2 = new ByteArrayInputStream("same".getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(IoUtil.contentEquals(in1, in2));
		InputStream in3 = new ByteArrayInputStream("diff".getBytes(StandardCharsets.UTF_8));
		Assert.assertFalse(IoUtil.contentEquals(in1, in3));
	}

	@Test
	public void testIoLinesAndClose() throws IOException {
		InputStream in = new ByteArrayInputStream("a\nb\nc".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(Arrays.asList("a", "b", "c"), IoUtil.readUtf8Lines(in));
		IoUtil.closeQuietly(null);
	}
}
