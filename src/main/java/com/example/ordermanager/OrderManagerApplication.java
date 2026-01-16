package com.example.ordermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrderManagerApplication {

	public static void main(String[] args) {
		System.out.println("Starting Order Manager Application...");
		SpringApplication.run(OrderManagerApplication.class, args);
	}

}
