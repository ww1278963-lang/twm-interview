package com.allen.interview.order.notification;

import java.math.BigDecimal;

import com.allen.interview.order.model.Order;

/** Immutable snapshot, so background threads never read a live Order. */
public record OrderPaidNotification(String orderId, String customerEmail, BigDecimal paidAmount) {

	public static OrderPaidNotification from(Order order) {
		return new OrderPaidNotification(order.getOrderId(), order.getCustomerEmail(), order.getDiscountedAmount());
	}
}
