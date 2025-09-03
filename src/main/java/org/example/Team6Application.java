package org.example;

import org.example.domain.Cancer;
import org.example.repository.CancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class Team6Application {

    @Autowired
    private CancerRepository cancerRepository;

    public static void main(String[] args) {
        SpringApplication.run(Team6Application.class, args);
    }


}

