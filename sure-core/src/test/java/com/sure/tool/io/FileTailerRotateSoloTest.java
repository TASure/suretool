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

package com.sure.tool.io;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

public class FileTailerRotateSoloTest {

	@Test
	public void 轮转重开新文件更大() throws Exception {
		Path dir = Files.createTempDirectory("rot-solo");
		Path file = dir.resolve("rot.log");
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer.forceNullFileKey = true;
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			Thread.sleep(150);
			Files.move(file, dir.resolve("rot.log.1"), StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			long deadline = System.currentTimeMillis() + 6000;
			while (System.currentTimeMillis() < deadline && lines.size() < 1) {
				Thread.sleep(20);
			}
			System.out.println("[solo] lines=" + lines);
			assertEquals(1, lines.size());
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
			FileTailer.forceNullFileKey = false;
		}
	}
}
