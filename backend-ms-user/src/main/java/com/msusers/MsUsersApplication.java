package com.msusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MsUsersApplication {

	public static void main(String[] args) {
		// Railway's internal interface takes some time to start. We wait for it to be ready.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		SpringApplication.run(MsUsersApplication.class, args);
	}
}
