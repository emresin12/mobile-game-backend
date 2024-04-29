package com.dreamgames.backendengineeringcasestudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class BackendEngineeringCaseStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendEngineeringCaseStudyApplication.class, args);

        System.out.println("**********************************************************************");
        System.out.println("************* Server is started. Listening port 8080 ... *************");
        System.out.println("**********************************************************************");
    }
}
