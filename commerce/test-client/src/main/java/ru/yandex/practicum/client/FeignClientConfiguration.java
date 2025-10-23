package ru.yandex.practicum.client;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignClientConfiguration {

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("test-client", "pass");
    }

}
