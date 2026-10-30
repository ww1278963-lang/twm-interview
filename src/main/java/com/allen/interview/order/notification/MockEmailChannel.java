package com.allen.interview.order.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default email channel; only logs. */
@Component
@ConditionalOnProperty(name = "app.notification.email.mode", havingValue = "mock", matchIfMissing = true)
public class MockEmailChannel implements NotificationChannel {

	private static final Logger log = LoggerFactory.getLogger(MockEmailChannel.class);

	@Override
	public String name() {
		return "email";
	}

	@Override
	public void send(OrderPaidNotification notification) {
		log.info("[mock email] to={} order={} paid={}", notification.customerEmail(), notification.orderId(),
				notification.paidAmount());
	}
}
