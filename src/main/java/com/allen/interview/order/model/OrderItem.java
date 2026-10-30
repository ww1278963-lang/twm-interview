package com.allen.interview.order.model;

import java.math.BigDecimal;

public record OrderItem(String productId, String productName, BigDecimal unitPrice, int quantity) {

	public OrderItem {
		if (productId == null || productId.isBlank()) {
			throw new IllegalArgumentException("productId is required");
		}
		if (productName == null || productName.isBlank()) {
			throw new IllegalArgumentException("productName is required");
		}
		if (unitPrice == null || unitPrice.signum() < 0) {
			throw new IllegalArgumentException("unitPrice must be >= 0");
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("quantity must be > 0");
		}
	}

	public BigDecimal getSubtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}
}
