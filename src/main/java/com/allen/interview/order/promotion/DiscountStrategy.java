package com.allen.interview.order.promotion;

import java.math.BigDecimal;

import com.allen.interview.order.model.Order;

/**
 * One promotion rule. To add a promotion, implement this as a {@code @Component} and
 * give it an {@code @Order}; no existing class needs to change.
 */
public interface DiscountStrategy {

	/** @return the amount after this discount, given the amount left by earlier strategies */
	BigDecimal apply(BigDecimal currentAmount, Order order);
}
