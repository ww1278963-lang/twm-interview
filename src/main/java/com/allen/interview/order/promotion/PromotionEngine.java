package com.allen.interview.order.promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.allen.interview.order.model.Order;

/** Runs every strategy in list order; rounds once at the end so errors don't accumulate. */
@Service
public class PromotionEngine {

	private final List<DiscountStrategy> strategies;

	public PromotionEngine(List<DiscountStrategy> strategies) {
		this.strategies = List.copyOf(strategies);
	}

	public BigDecimal calculateDiscountedAmount(Order order) {
		BigDecimal amount = order.getTotalAmount();
		for (DiscountStrategy strategy : strategies) {
			amount = strategy.apply(amount, order);
		}
		return amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}
}
