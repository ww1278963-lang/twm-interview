package com.allen.interview.order.notification;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Sends through every channel in parallel; one channel failing affects neither the others nor the caller. */
@Service
public class NotificationDispatcher {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

	private final List<NotificationChannel> channels;
	private final ExecutorService executor;

	public NotificationDispatcher(List<NotificationChannel> channels, ExecutorService notificationExecutor) {
		this.channels = List.copyOf(channels);
		this.executor = notificationExecutor;
	}

	public CompletableFuture<Void> dispatch(OrderPaidNotification notification) {
		CompletableFuture<?>[] sends = channels.stream()
				.map(channel -> CompletableFuture.runAsync(() -> channel.send(notification), executor)
						.exceptionally(ex -> {
							log.error("{} notification failed for order {}", channel.name(), notification.orderId(), ex);
							return null;
						}))
				.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(sends);
	}
}
