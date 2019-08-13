package com.tonkar.volleyballreferee;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableEncryptableProperties
public class VolleyballRefereeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolleyballRefereeApplication.class, args);
	}
}
