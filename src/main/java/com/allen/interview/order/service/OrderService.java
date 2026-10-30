package com.allen.interview.order.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.allen.interview.order.model.MemberLevel;
import com.allen.interview.order.model.Order;
import com.allen.interview.order.model.OrderItem;
import com.allen.interview.order.model.OrderStatus;
import com.allen.interview.order.notification.NotificationDispatcher;
import com.allen.interview.order.notification.OrderPaidNotification;
import com.allen.interview.order.promotion.PromotionEngine;

@Service
public class OrderService {

	private final InMemoryOrderRepository repository;
	private final PromotionEngine promotionEngine;
	private final NotificationDispatcher notificationDispatcher;

	public OrderService(InMemoryOrderRepository repository, PromotionEngine promotionEngine,
			NotificationDispatcher notificationDispatcher) {
		this.repository = repository;
		this.promotionEngine = promotionEngine;
		this.notificationDispatcher = notificationDispatcher;
	}

	public Order createOrder(List<OrderItem> items, MemberLevel memberLevel, LocalDate orderDate, String customerEmail) {
		Order order = new Order(UUID.randomUUID().toString(), items, memberLevel, orderDate, customerEmail);
		order.applyDiscountedAmount(promotionEngine.calculateDiscountedAmount(order));
		repository.save(order);
		return order;
	}

	/** Returns without waiting for notifications. Paying twice throws, so nothing is sent twice. */
	public Order pay(String orderId) {
		Order order = getOrder(orderId);
		order.transitionTo(OrderStatus.PAID);
		notificationDispatcher.dispatch(OrderPaidNotification.from(order));
		return order;
	}

	public Order ship(String orderId) {
		Order order = getOrder(orderId);
		order.transitionTo(OrderStatus.SHIPPED);
		return order;
	}

	public Order getOrder(String orderId) {
		return repository.findById(orderId)
				.orElseThrow(() -> new NoSuchElementException("order not found: " + orderId));
	}
}
