package com.allen.interview.order.notification;

public interface NotificationChannel {

	String name();

	void send(OrderPaidNotification notification);
}
