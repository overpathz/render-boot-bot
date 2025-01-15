package com.example.rendertestbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RenderTestBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenderTestBotApplication.class, args);
    }

}
