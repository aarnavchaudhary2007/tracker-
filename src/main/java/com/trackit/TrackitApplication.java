package com.trackit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrackitApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackitApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  TrackIt is running!");
        System.out.println("  API:     http://localhost:8080/api/applications");
        System.out.println("  Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("  H2 DB:   http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
