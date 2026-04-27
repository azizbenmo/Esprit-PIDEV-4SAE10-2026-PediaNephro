package com.pedianephro.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.pedianephro.subscription.client")
@EnableScheduling
public class SubscriptionApplication {
	public static void main(String[] args) {
		SpringApplication.run(SubscriptionApplication.class, args);
	}
}
