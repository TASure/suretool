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

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * 邮件发送工具（基于 jakarta.mail / Angus Mail）。
 * <p>
 * 支持文本与 HTML 正文、多个收件人、附件；可配置 SMTP 主机、端口、
 * SSL/TLS 与 STARTTLS。测试环境可将主机指向本地假 SMTP 服务器。
 *
 * @author suretool
 * @since 1.1.0
 */
public class MailUtil {

	private MailUtil() {
	}

	/**
	 * 发送纯文本邮件。
	 *
	 * @param account 邮箱账户配置
	 * @param to      收件人（可多个）
	 * @param subject 主题
	 * @param content 正文
	 * @throws MessagingException 发送失败
	 */
	public static void sendText(MailAccount account, List<String> to, String subject,
			String content) throws MessagingException {
		send(account, to, subject, content, false, null);
	}

	/**
	 * 发送 HTML 邮件。
	 *
	 * @param account 邮箱账户配置
	 * @param to      收件人（可多个）
	 * @param subject 主题
	 * @param html    HTML 正文
	 * @throws MessagingException 发送失败
	 */
	public static void sendHtml(MailAccount account, List<String> to, String subject,
			String html) throws MessagingException {
		send(account, to, subject, html, true, null);
	}

	/**
	 * 发送带附件的邮件。
	 *
	 * @param account     邮箱账户配置
	 * @param to          收件人（可多个）
	 * @param subject     主题
	 * @param content     正文（纯文本）
	 * @param attachments 附件文件
	 * @throws MessagingException 发送失败
	 */
	public static void sendWithAttachments(MailAccount account, List<String> to, String subject,
			String content, List<File> attachments) throws MessagingException {
		send(account, to, subject, content, false, attachments);
	}

	private static void send(MailAccount account, List<String> to, String subject,
			String content, boolean html, List<File> attachments) throws MessagingException {
		if (account == null || account.getHost() == null || account.getHost().isEmpty()) {
			throw new IllegalArgumentException("SMTP 主机不能为空");
		}
		Session session = Session.getInstance(properties(account));
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(account.getFrom(), true));
		for (String recipient : to) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, true));
		}
		message.setSubject(subject, "UTF-8");
		if (attachments == null || attachments.isEmpty()) {
			message.setContent(content, html ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");
		} else {
			MimeMultipart multipart = new MimeMultipart();
			MimeBodyPart body = new MimeBodyPart();
			body.setContent(content, html ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");
			multipart.addBodyPart(body);
			for (File file : attachments) {
				MimeBodyPart part = new MimeBodyPart();
				try {
					part.attachFile(file);
				} catch (java.io.IOException e) {
					throw new MessagingException("附件读取失败: " + file, e);
				}
				multipart.addBodyPart(part);
			}
			message.setContent(multipart);
		}
		message.saveChanges();
		try (Transport transport = session.getTransport("smtp")) {
			transport.connect(account.getHost(), account.getPort(),
					account.getUsername(), account.getPassword());
			transport.sendMessage(message, message.getAllRecipients());
		}
	}

	private static Properties properties(MailAccount account) {
		Properties props = new Properties();
		props.put("mail.smtp.host", account.getHost());
		props.put("mail.smtp.port", String.valueOf(account.getPort()));
		props.put("mail.smtp.auth", String.valueOf(
				account.getUsername() != null && !account.getUsername().isEmpty()));
		props.put("mail.smtp.connectiontimeout", "10000");
		props.put("mail.smtp.timeout", "10000");
		if (account.isSsl()) {
			props.put("mail.smtp.ssl.enable", "true");
		}
		if (account.isStarttls()) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		return props;
	}
}
