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
 * sure-math：数学工具门面。
 *
 * <p>提供素数、公约数/公倍数、阶乘/组合、进制与位运算、BigDecimal 精确运算、
 * 随机数与数字解析等常用数学能力，全部无状态静态门面，线程安全，零第三方依赖。</p>
 */
module sure.math {
	requires transitive sure.core;

	exports com.sure.tool.math;
}
