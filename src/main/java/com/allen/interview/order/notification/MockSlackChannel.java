package com.allen.interview.order.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockSlackChannel implements NotificationChannel {

	private static final Logger log = LoggerFactory.getLogger(MockSlackChannel.class);

	@Override
	public String name() {
		return "slack";
	}

	@Override
	public void send(OrderPaidNotification notification) {
		log.info("[mock slack] order={} paid={}", notification.orderId(), notification.paidAmount());
	}
}
