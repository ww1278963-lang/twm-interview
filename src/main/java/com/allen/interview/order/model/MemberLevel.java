package com.allen.interview.order.model;

import java.math.BigDecimal;

public enum MemberLevel {
	BRONZE("1.00"), SILVER("0.95"), GOLD("0.90");

	private final BigDecimal rate;

	MemberLevel(String rate) {
		this.rate = new BigDecimal(rate);
	}

	public BigDecimal getRate() {
		return rate;
	}
}
