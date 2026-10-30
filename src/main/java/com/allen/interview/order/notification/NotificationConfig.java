package com.allen.interview.order.notification;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

	/** Sending is blocking I/O, which is the case virtual threads are made for. */
	@Bean(destroyMethod = "close")
	public ExecutorService notificationExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
