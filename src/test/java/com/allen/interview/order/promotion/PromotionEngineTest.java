package com.allen.interview.order.promotion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.allen.interview.order.model.MemberLevel;
import com.allen.interview.order.model.Order;
import com.allen.interview.order.model.OrderItem;

class PromotionEngineTest {

	private static final LocalDate NORMAL_DAY = LocalDate.of(2026, 3, 1);

	private final PromotionEngine engine = new PromotionEngine(List.of(new ThresholdDiscountStrategy(),
			new HolidayDiscountStrategy(), new MemberLevelDiscountStrategy()));

	private static Order order(String total, MemberLevel level, LocalDate date) {
		return new Order("O1", List.of(new OrderItem("P1", "Phone", new BigDecimal(total), 1)), level, date,
				"a@example.com");
	}

	@ParameterizedTest
	@CsvSource({ "999.99, 999.99", "1000, 900", "1999.99, 1899.99", "2000, 1750", "5000, 4750" })
	void thresholdTiers(String total, String expected) {
		assertThat(engine.calculateDiscountedAmount(order(total, MemberLevel.BRONZE, NORMAL_DAY)))
				.isEqualByComparingTo(expected);
	}

	@ParameterizedTest
	@CsvSource({ "2026-11-11, 450", "2030-12-25, 450", "2026-11-12, 500", "2026-12-24, 500" })
	void holidays(LocalDate date, String expected) {
		assertThat(engine.calculateDiscountedAmount(order("500", MemberLevel.BRONZE, date)))
				.isEqualByComparingTo(expected);
	}

	@ParameterizedTest
	@CsvSource({ "BRONZE, 500", "SILVER, 475", "GOLD, 450" })
	void memberLevels(MemberLevel level, String expected) {
		assertThat(engine.calculateDiscountedAmount(order("500", level, NORMAL_DAY))).isEqualByComparingTo(expected);
	}

	@Test
	void allDiscountsStackInOrder() {
		// (2000 - 250) * 0.9 * 0.9
		assertThat(engine.calculateDiscountedAmount(order("2000", MemberLevel.GOLD, LocalDate.of(2026, 11, 11))))
				.isEqualByComparingTo("1417.50");
	}

	@Test
	void resultIsRoundedToTwoDecimals() {
		// 10.01 * 0.95 = 9.5095
		assertThat(engine.calculateDiscountedAmount(order("10.01", MemberLevel.SILVER, NORMAL_DAY)))
				.isEqualTo(new BigDecimal("9.51"));
	}

	@Test
	void newStrategyPlugsInWithoutChangingTheEngine() {
		DiscountStrategy tenOff = (amount, order) -> amount.subtract(BigDecimal.TEN);
		PromotionEngine extended = new PromotionEngine(List.of(tenOff));
		assertThat(extended.calculateDiscountedAmount(order("100", MemberLevel.BRONZE, NORMAL_DAY)))
				.isEqualByComparingTo("90");
	}

	@Test
	void amountNeverGoesNegative() {
		DiscountStrategy hugeOff = (amount, order) -> amount.subtract(new BigDecimal("1000"));
		PromotionEngine extended = new PromotionEngine(List.of(hugeOff));
		assertThat(extended.calculateDiscountedAmount(order("100", MemberLevel.BRONZE, NORMAL_DAY)))
				.isEqualByComparingTo("0");
	}
}
