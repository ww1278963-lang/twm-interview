package com.allen.interview.order.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderTest {

	private static OrderItem item(String price, int quantity) {
		return new OrderItem("P1", "Phone", new BigDecimal(price), quantity);
	}

	private static Order order(OrderItem... items) {
		return new Order("O1", List.of(items), MemberLevel.BRONZE, LocalDate.of(2026, 1, 1), "a@example.com");
	}

	@Test
	void subtotalAndTotalAreExactDecimals() {
		// 0.1 * 3 is 0.30000000000000004 in double
		assertThat(item("0.1", 3).getSubtotal()).isEqualByComparingTo("0.3");
		assertThat(order(item("0.1", 3), item("19.99", 2)).calculateTotal()).isEqualByComparingTo("40.28");
	}

	@Test
	void invalidItemsAreRejected() {
		assertThatThrownBy(() -> item("-1", 1)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> item("1", 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new OrderItem(" ", "Phone", BigDecimal.ONE, 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void emptyOrderIsRejected() {
		assertThatThrownBy(() -> order()).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void statusOnlyMovesForward() {
		Order order = order(item("10", 1));
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

		assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPED)).isInstanceOf(IllegalStateException.class);

		order.transitionTo(OrderStatus.PAID);
		order.transitionTo(OrderStatus.SHIPPED);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

		assertThatThrownBy(() -> order.transitionTo(OrderStatus.PAID)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void amountIsFrozenOncePaid() {
		Order order = order(item("10", 1));
		order.transitionTo(OrderStatus.PAID);
		assertThatThrownBy(() -> order.applyDiscountedAmount(BigDecimal.ONE)).isInstanceOf(IllegalStateException.class);
	}
}
