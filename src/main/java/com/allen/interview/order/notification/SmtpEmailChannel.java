package com.allen.interview.order.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Real delivery, enabled with app.notification.email.mode=smtp plus the spring.mail.* settings. */
@Component
@ConditionalOnProperty(name = "app.notification.email.mode", havingValue = "smtp")
public class SmtpEmailChannel implements NotificationChannel {

	private final JavaMailSender mailSender;
	private final String from;

	public SmtpEmailChannel(JavaMailSender mailSender, @Value("${app.notification.email.from}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public String name() {
		return "email";
	}

	@Override
	public void send(OrderPaidNotification notification) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(notification.customerEmail());
		message.setSubject("Order " + notification.orderId() + " paid");
		message.setText("We received your payment of $" + notification.paidAmount() + " for order "
				+ notification.orderId() + ".");
		mailSender.send(message);
	}
}
