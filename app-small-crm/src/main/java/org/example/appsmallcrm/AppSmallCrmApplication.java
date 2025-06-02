package org.example.appsmallcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class AppSmallCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppSmallCrmApplication.class, args);
    }

}
