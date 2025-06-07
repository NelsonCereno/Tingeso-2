package com.karting.descuentopersonasservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class DescuentoPersonasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DescuentoPersonasServiceApplication.class, args);
    }
}
