package com.mscommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MsCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCommerceApplication.class, args);
    }

}
