package com.allen.interview.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.allen.interview.order.model.MemberLevel;
import com.allen.interview.order.model.Order;
import com.allen.interview.order.model.OrderItem;
import com.allen.interview.order.service.OrderService;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

/** Full path with smtp mode switched on: real wiring, pay(), background send, mail received. */
@SpringBootTest(properties = { "app.notification.email.mode=smtp", "app.notification.email.from=shop@example.com",
		"spring.mail.host=localhost", "spring.mail.port=3025" })
class OrderPaidEmailIntegrationTest {

	@RegisterExtension
	static final GreenMailExtension smtpServer = new GreenMailExtension(ServerSetupTest.SMTP);

	@Autowired
	private OrderService orderService;

	@Test
	void payingAnOrderDeliversAnEmail() throws Exception {
		// (2000 - 250) * 0.9 * 0.9, on Double 11 as a GOLD member
		Order order = orderService.createOrder(List.of(new OrderItem("P1", "Phone", new BigDecimal("2000"), 1)),
				MemberLevel.GOLD, LocalDate.of(2026, 11, 11), "buyer@example.com");

		orderService.pay(order.getOrderId());

		assertThat(smtpServer.waitForIncomingEmail(5000, 1)).isTrue();
		assertThat(smtpServer.getReceivedMessages()[0].getContent().toString()).contains("1417.50");
	}
}
