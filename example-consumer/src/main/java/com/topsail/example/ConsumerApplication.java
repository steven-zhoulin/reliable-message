package com.topsail.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Steven
 * @date 2020-05-06
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ConsumerApplication {

    public static void main(String[] args){
        SpringApplication.run(ConsumerApplication.class, args);
    }

}
