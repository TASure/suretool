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
module sure.core {
	requires java.management;
	requires java.sql;
	requires java.desktop;
	requires jdk.management;
	exports com.sure.tool.bean;
	exports com.sure.tool.codec;
	exports com.sure.tool.collection;
	exports com.sure.tool.config;
	exports com.sure.tool.date;
	exports com.sure.tool.graph;
	exports com.sure.tool.id;
	exports com.sure.tool.image;
	exports com.sure.tool.io;
	exports com.sure.tool.lang;
	exports com.sure.tool.system;
	exports com.sure.tool.text;
	exports com.sure.tool.thread;
	exports com.sure.tool.util;
	opens com.sure.tool.bean;
	opens com.sure.tool.codec;
	opens com.sure.tool.collection;
	opens com.sure.tool.config;
	opens com.sure.tool.date;
	opens com.sure.tool.graph;
	opens com.sure.tool.id;
	opens com.sure.tool.image;
	opens com.sure.tool.io;
	opens com.sure.tool.lang;
	opens com.sure.tool.system;
	opens com.sure.tool.text;
	opens com.sure.tool.thread;
	opens com.sure.tool.util;
}
