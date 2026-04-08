package com.notification_service.infra.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfiguration {
    public static final String TRANSACTION_COMPLETED_QUEUE = "transaction.completed";


    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
