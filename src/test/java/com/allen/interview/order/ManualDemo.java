package com.allen.interview.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.allen.interview.order.model.MemberLevel;
import com.allen.interview.order.model.Order;
import com.allen.interview.order.model.OrderItem;
import com.allen.interview.order.service.OrderService;

/** For trying things by hand: edit the values below and run. Not picked up by a plain mvn test. */
@SpringBootTest
class ManualDemo {

	private static final String UNIT_PRICE = "2000";
	private static final int QUANTITY = 1;
	private static final MemberLevel LEVEL = MemberLevel.GOLD;
	private static final LocalDate ORDER_DATE = LocalDate.of(2026, 11, 11);
	private static final String CUSTOMER_EMAIL = "buyer@example.com";

	@Autowired
	private OrderService orderService;

	@Test
	void run() throws InterruptedException {
		Order order = orderService.createOrder(
				List.of(new OrderItem("P1", "Phone", new BigDecimal(UNIT_PRICE), QUANTITY)), LEVEL, ORDER_DATE,
				CUSTOMER_EMAIL);
		System.out.println(">>> total      = " + order.getTotalAmount());
		System.out.println(">>> discounted = " + order.getDiscountedAmount());
		System.out.println(">>> status     = " + order.getStatus());

		orderService.pay(order.getOrderId());
		System.out.println(">>> after pay  = " + order.getStatus());

		// Notifications run in the background; give them time to print before the JVM exits.
		Thread.sleep(3000);

		try {
			orderService.pay(order.getOrderId());
		}
		catch (IllegalStateException ex) {
			System.out.println(">>> pay again  = rejected: " + ex.getMessage());
		}

		orderService.ship(order.getOrderId());
		System.out.println(">>> after ship = " + order.getStatus());
	}
}
