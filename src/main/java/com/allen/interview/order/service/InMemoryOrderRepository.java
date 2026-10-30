package com.allen.interview.order.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.allen.interview.order.model.Order;

@Repository
public class InMemoryOrderRepository {

	private final Map<String, Order> orders = new ConcurrentHashMap<>();

	public void save(Order order) {
		orders.put(order.getOrderId(), order);
	}

	public Optional<Order> findById(String orderId) {
		return Optional.ofNullable(orders.get(orderId));
	}
}
