package com.allen.interview.order.promotion;

import java.math.BigDecimal;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class MemberLevelDiscountStrategy implements DiscountStrategy {

	@Override
	public BigDecimal apply(BigDecimal currentAmount, com.allen.interview.order.model.Order order) {
		return currentAmount.multiply(order.getMemberLevel().getRate());
	}
}
