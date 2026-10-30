package com.allen.interview.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.allen.interview.order.model.MemberLevel;
import com.allen.interview.order.model.Order;
import com.allen.interview.order.model.OrderItem;
import com.allen.interview.order.model.OrderStatus;
import com.allen.interview.order.notification.NotificationChannel;
import com.allen.interview.order.notification.NotificationDispatcher;
import com.allen.interview.order.notification.OrderPaidNotification;
import com.allen.interview.order.promotion.MemberLevelDiscountStrategy;
import com.allen.interview.order.promotion.PromotionEngine;

class OrderServiceTest {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	@AfterEach
	void closeExecutor() {
		executor.shutdownNow();
	}

	private OrderService serviceWith(NotificationChannel... channels) {
		return new OrderService(new InMemoryOrderRepository(),
				new PromotionEngine(List.of(new MemberLevelDiscountStrategy())),
				new NotificationDispatcher(List.of(channels), executor));
	}

	private static Order newOrder(OrderService service) {
		return service.createOrder(List.of(new OrderItem("P1", "Phone", new BigDecimal("100"), 2)), MemberLevel.GOLD,
				LocalDate.of(2026, 3, 1), "a@example.com");
	}

	/** Records what it receives and counts down, so tests can wait for background sends. */
	private static final class RecordingChannel implements NotificationChannel {
		final List<OrderPaidNotification> received = new CopyOnWriteArrayList<>();
		final CountDownLatch sent = new CountDownLatch(1);

		@Override
		public String name() {
			return "recording";
		}

		@Override
		public void send(OrderPaidNotification notification) {
			received.add(notification);
			sent.countDown();
		}
	}

	@Test
	void createOrderStoresTotalAndDiscountedAmount() {
		OrderService service = serviceWith();
		Order order = newOrder(service);

		assertThat(order.getTotalAmount()).isEqualByComparingTo("200");
		assertThat(order.getDiscountedAmount()).isEqualByComparingTo("180");
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(service.getOrder(order.getOrderId())).isSameAs(order);
	}

	@Test
	void payNotifiesEveryChannelWithThePaidAmount() throws InterruptedException {
		RecordingChannel email = new RecordingChannel();
		RecordingChannel slack = new RecordingChannel();
		OrderService service = serviceWith(email, slack);
		Order order = newOrder(service);

		service.pay(order.getOrderId());

		assertThat(email.sent.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(slack.sent.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(email.received).singleElement().satisfies(n -> {
			assertThat(n.orderId()).isEqualTo(order.getOrderId());
			assertThat(n.customerEmail()).isEqualTo("a@example.com");
			assertThat(n.paidAmount()).isEqualByComparingTo("180");
		});
		assertThat(slack.received).hasSize(1);
	}

	@Test
	void payDoesNotWaitForASlowChannel() throws InterruptedException {
		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		NotificationChannel slow = new NotificationChannel() {
			@Override
			public String name() {
				return "slow";
			}

			@Override
			public void send(OrderPaidNotification notification) {
				try {
					release.await();
					finished.countDown();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		};
		OrderService service = serviceWith(slow);

		// Reaching the assertion at all proves pay() returned while the channel is still blocked.
		Order paid = service.pay(newOrder(service).getOrderId());

		assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(finished.getCount()).isEqualTo(1);
		release.countDown();
		assertThat(finished.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void oneFailingChannelDoesNotAffectTheOtherOrThePayment() throws InterruptedException {
		NotificationChannel broken = new NotificationChannel() {
			@Override
			public String name() {
				return "broken";
			}

			@Override
			public void send(OrderPaidNotification notification) {
				throw new IllegalStateException("smtp down");
			}
		};
		RecordingChannel slack = new RecordingChannel();
		OrderService service = serviceWith(broken, slack);

		Order paid = service.pay(newOrder(service).getOrderId());

		assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(slack.sent.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void concurrentPaymentsSucceedAndNotifyOnlyOnce() throws Exception {
		RecordingChannel channel = new RecordingChannel();
		OrderService service = serviceWith(channel);
		String orderId = newOrder(service).getOrderId();

		int attempts = 50;
		CountDownLatch start = new CountDownLatch(1);
		Callable<Order> attempt = () -> {
			start.await();
			return service.pay(orderId);
		};
		List<Future<Order>> futures = new ArrayList<>();
		for (int i = 0; i < attempts; i++) {
			futures.add(executor.submit(attempt));
		}
		start.countDown();

		int succeeded = 0;
		for (Future<Order> future : futures) {
			try {
				future.get(5, TimeUnit.SECONDS);
				succeeded++;
			}
			catch (ExecutionException ex) {
				assertThat(ex).hasCauseInstanceOf(IllegalStateException.class);
			}
		}

		assertThat(succeeded).isEqualTo(1);
		assertThat(channel.sent.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(channel.received).hasSize(1);
	}

	@Test
	void unknownOrderIsRejected() {
		assertThatThrownBy(() -> serviceWith().pay("nope")).isInstanceOf(NoSuchElementException.class);
	}
}
