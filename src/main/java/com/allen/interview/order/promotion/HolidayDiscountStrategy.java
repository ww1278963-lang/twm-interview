package com.allen.interview.order.promotion;

import java.math.BigDecimal;
import java.time.Month;
import java.time.MonthDay;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class HolidayDiscountStrategy implements DiscountStrategy {

	private static final Set<MonthDay> HOLIDAYS = Set.of(
			MonthDay.of(Month.NOVEMBER, 11),
			MonthDay.of(Month.DECEMBER, 25));
	private static final BigDecimal RATE = new BigDecimal("0.90");

	@Override
	public BigDecimal apply(BigDecimal currentAmount, com.allen.interview.order.model.Order order) {
		return HOLIDAYS.contains(MonthDay.from(order.getOrderDate())) ? currentAmount.multiply(RATE) : currentAmount;
	}
}
