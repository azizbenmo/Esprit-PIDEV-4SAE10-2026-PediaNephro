package com.example.consultation_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class ConsultationMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsultationMicroserviceApplication.class, args);
	}

}
