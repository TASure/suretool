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
/**
 * sure-compress：高压缩率格式门面。
 *
 * <p>提供 7z（LZMA2，基于 Apache commons-compress）的目录压缩/解压，
 * 以及 Brotli 流解码与格式检测（官方 org.brotli:dec，纯 Java）。
 * Zip/Gzip 由 sure-core 的 ZipUtil/GzipUtil 覆盖。</p>
 */
module sure.compress {
	requires transitive sure.core;
	requires org.apache.commons.compress; // commons-compress 1.27.1 (Automatic-Module-Name)
	requires org.tukaani.xz; // xz 1.10 的 META-INF/versions/9/module-info.class 声明模块名
	requires dec; // org.brotli:dec 无 Automatic-Module-Name，自动模块名为 dec
	requires static com.github.spotbugs.annotations;

	exports com.sure.tool.compress;
}
