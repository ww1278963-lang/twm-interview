package com.allen.interview.order.promotion;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Only the highest tier reached applies: $2,000 takes $250 off, not $350. */
@Component
@Order(1)
public class ThresholdDiscountStrategy implements DiscountStrategy {

	private record Tier(BigDecimal threshold, BigDecimal discount) {
	}

	private static final List<Tier> TIERS_DESCENDING = List.of(
			new Tier(new BigDecimal("2000"), new BigDecimal("250")),
			new Tier(new BigDecimal("1000"), new BigDecimal("100")));

	@Override
	public BigDecimal apply(BigDecimal currentAmount, com.allen.interview.order.model.Order order) {
		return TIERS_DESCENDING.stream()
				.filter(tier -> currentAmount.compareTo(tier.threshold()) >= 0)
				.findFirst()
				.map(tier -> currentAmount.subtract(tier.discount()))
				.orElse(currentAmount);
	}
}
