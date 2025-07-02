package com.autoalert.autoalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
@EnableScheduling
@SpringBootApplication
public class AutoAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoAlertApplication.class, args);
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Bucharest"));
	}

}
