package com.tonkar.volleyballreferee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class VolleyballRefereeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolleyballRefereeApplication.class, args);
    }
}
