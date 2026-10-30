package com.allen.interview.order.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

/** Talks real SMTP to an in-process server, so this exercises actual delivery. */
class SmtpEmailChannelTest {

	@RegisterExtension
	static final GreenMailExtension smtpServer = new GreenMailExtension(ServerSetupTest.SMTP);

	@Test
	void sendsARealMessageOverSmtp() throws Exception {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");
		mailSender.setPort(smtpServer.getSmtp().getPort());
		SmtpEmailChannel channel = new SmtpEmailChannel(mailSender, "shop@example.com");

		channel.send(new OrderPaidNotification("O1", "buyer@example.com", new BigDecimal("180.00")));

		MimeMessage[] received = smtpServer.getReceivedMessages();
		assertThat(received).hasSize(1);
		assertThat(received[0].getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("buyer@example.com");
		assertThat(received[0].getFrom()[0].toString()).isEqualTo("shop@example.com");
		assertThat(received[0].getSubject()).contains("O1");
		assertThat(received[0].getContent().toString()).contains("180.00");
	}
}
