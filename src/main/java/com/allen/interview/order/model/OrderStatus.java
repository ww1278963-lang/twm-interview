package com.allen.interview.order.model;

public enum OrderStatus {
	PENDING, PAID, SHIPPED;

	public boolean canTransitionTo(OrderStatus next) {
		return switch (this) {
			case PENDING -> next == PAID;
			case PAID -> next == SHIPPED;
			case SHIPPED -> false;
		};
	}
}
