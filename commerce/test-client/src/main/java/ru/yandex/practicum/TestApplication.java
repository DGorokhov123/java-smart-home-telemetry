package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(TestApplication.class, args);
    }

}
