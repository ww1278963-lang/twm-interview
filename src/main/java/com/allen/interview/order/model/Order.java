package com.allen.interview.order.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Order {

	private final String orderId;
	private final List<OrderItem> items;
	private final MemberLevel memberLevel;
	private final LocalDate orderDate;
	private final String customerEmail;
	private final BigDecimal totalAmount;
	/** Amount payable after discounts, not the amount taken off. */
	private BigDecimal discountedAmount;
	private OrderStatus status = OrderStatus.PENDING;

	public Order(String orderId, List<OrderItem> items, MemberLevel memberLevel, LocalDate orderDate,
			String customerEmail) {
		if (orderId == null || orderId.isBlank()) {
			throw new IllegalArgumentException("orderId is required");
		}
		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("order must contain at least one item");
		}
		if (memberLevel == null) {
			throw new IllegalArgumentException("memberLevel is required");
		}
		if (orderDate == null) {
			throw new IllegalArgumentException("orderDate is required");
		}
		if (customerEmail == null || !customerEmail.contains("@")) {
			throw new IllegalArgumentException("customerEmail is invalid");
		}
		this.orderId = orderId;
		this.items = List.copyOf(items);
		this.memberLevel = memberLevel;
		this.orderDate = orderDate;
		this.customerEmail = customerEmail;
		this.totalAmount = calculateTotal();
		this.discountedAmount = totalAmount;
	}

	public BigDecimal calculateTotal() {
		return items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public synchronized void applyDiscountedAmount(BigDecimal amount) {
		if (status != OrderStatus.PENDING) {
			throw new IllegalStateException("cannot change amount of a " + status + " order");
		}
		if (amount == null || amount.signum() < 0 || amount.compareTo(totalAmount) > 0) {
			throw new IllegalArgumentException("discounted amount must be between 0 and the total");
		}
		this.discountedAmount = amount;
	}

	/** Atomic, so that of two concurrent payments only one succeeds and notifies. */
	public synchronized void transitionTo(OrderStatus next) {
		if (!status.canTransitionTo(next)) {
			throw new IllegalStateException("illegal status transition: " + status + " -> " + next);
		}
		this.status = next;
	}

	public String getOrderId() {
		return orderId;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public MemberLevel getMemberLevel() {
		return memberLevel;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public synchronized BigDecimal getDiscountedAmount() {
		return discountedAmount;
	}

	public synchronized OrderStatus getStatus() {
		return status;
	}
}
