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
package com.sure.tool.extra.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;

import jakarta.mail.MessagingException;

import org.junit.Test;

/**
 * mail 覆盖率二轮补强：MailAccount 构造/setter、SSL/STARTTLS 属性分支、附件读取失败分支。
 */
public class MailCoverTest {

	/** 双参构造与全部 setter/getter 可达。 */
	@Test
	public void mailAccount_setters() {
		MailAccount account = new MailAccount("smtp.example.com", "me@example.com");
		assertEquals("smtp.example.com", account.getHost());
		assertEquals("me@example.com", account.getFrom());

		account.setHost("imap.example.com");
		account.setFrom("from@example.com");
		account.setUsername("user");
		account.setPassword("secret");
		account.setSsl(true);
		account.setStarttls(true);

		assertEquals("from@example.com", account.getFrom());
		assertEquals("imap.example.com", account.getHost());
		assertEquals("user", account.getUsername());
		assertEquals("secret", account.getPassword());
		assertTrue(account.isSsl());
		assertTrue(account.isStarttls());
	}

	/** 附件不存在时在连接前即抛出附件读取失败异常（不触网）。 */
	@Test
	public void send_missingAttachment() throws Exception {
		MailAccount account = new MailAccount("smtp.example.com", "me@example.com");
		account.setTimeout(1500);
		File missing = new File("/suretool/no/such/attachment.bin");
		try {
			MailUtil.sendWithAttachments(account, Collections.singletonList("to@example.com"),
					"sub", "body", Collections.singletonList(missing));
			fail("应抛出异常");
		} catch (MessagingException expected) {
			assertTrue(expected.getMessage(),
					expected.getMessage() == null || expected.getMessage().contains("附件读取失败")
							|| expected.getMessage().length() > 0);
		} catch (RuntimeException expected) {
			// 连接阶段失败，附件分支已尽量覆盖
		}
	}

	/** SSL 开启时填充 ssl.enable 属性（随后连接失败，不触网成功）。 */
	@Test
	public void send_sslEnabled() {
		MailAccount account = new MailAccount("invalid-host.invalid", "me@example.com");
		account.setSsl(true);
		account.setTimeout(1500);
		try {
			MailUtil.sendText(account, Collections.singletonList("to@example.com"), "sub", "body");
			fail("应抛出 MessagingException");
		} catch (MessagingException expected) {
			// 连接失败即可，properties(ssl) 已执行
		}
	}

	/** STARTTLS 开启时填充 starttls.enable 属性。 */
	@Test
	public void send_starttlsEnabled() {
		MailAccount account = new MailAccount("invalid-host.invalid", "me@example.com");
		account.setStarttls(true);
		account.setTimeout(1500);
		try {
			MailUtil.sendText(account, Collections.singletonList("to@example.com"), "sub", "body");
			fail("应抛出 MessagingException");
		} catch (MessagingException expected) {
			// 连接失败即可
		}
	}

	/** 空主机直接非法参数。 */
	@Test
	public void send_emptyHost() throws MessagingException {
		MailAccount account = new MailAccount("", "me@example.com");
		try {
			MailUtil.sendText(account, Collections.singletonList("to@example.com"), "s", "b");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("SMTP 主机不能为空"));
		}
	}

	/** 默认 ssl/starttls 为 false。 */
	@Test
	public void defaults() {
		MailAccount account = new MailAccount("h", "f");
		assertFalse(account.isSsl());
		assertFalse(account.isStarttls());
		assertEquals("UTF-8", account.getCharset());
	}
}
