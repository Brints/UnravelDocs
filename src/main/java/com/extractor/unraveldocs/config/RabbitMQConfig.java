package com.extractor.unraveldocs.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "unraveldocs-ocr-exchange";
    public static final String QUEUE_NAME = "unraveldocs-ocr-queue";
    public static final String ROUTING_KEY = "unraveldocs.ocr";

    @Bean
    public TopicExchange ocrExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue ocrQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding ocrBinding(Queue ocrQueue, TopicExchange ocrExchange) {
        return BindingBuilder.bind(ocrQueue).to(ocrExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
