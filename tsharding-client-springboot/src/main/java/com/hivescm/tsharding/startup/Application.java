package com.hivescm.tsharding.startup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
////import org.springframework.context.annotation.ImportResource;
//
//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
////@ImportResource(locations = { "classpath:spring-tsharding.xml" })
//@ComponentScan("com.hivescm")
public class Application {
	public static void main(String[] args) {
		ApplicationContext context= SpringApplication.run(Application.class, args);
		System.out.println(context);
	}
}
