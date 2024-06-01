package com.mscommerce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class MsCommerceApplication {

	public static void main(String[] args) {
		// Railway's internal interface takes some time to start. We wait for it to be ready.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		SpringApplication.run(MsCommerceApplication.class, args);
	}
}
