package com.b2g.recomendationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.List;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.name}")
    private String servicePrefix;

    @Value("#{'${app.rabbitmq.routing-keys}'.split(',')}")
    private List<String> patternBindingKeys;


    @Bean
    public TopicExchange b2gExchange() {
        return new TopicExchange(exchangeName, true, false);
    }


    @Bean
    public Queue bookQueue() {
        return new Queue("recommendation.book.queue", true, false, false);
    }

    @Bean
    public Queue userQueue() {
        return new Queue("recommendation.user.queue", true, false, false);
    }

    @Bean
    public Queue reviewQueue() {
        return new Queue("recommendation.review.queue", true, false, false);
    }

    @Bean
    public Binding bookBinding(TopicExchange b2gExchange, Queue bookQueue) {
        return BindingBuilder.bind(bookQueue)
                .to(b2gExchange)
                .with("book.#");
    }

    @Bean
    public Binding userBinding(TopicExchange b2gExchange, Queue userQueue) {
        return BindingBuilder.bind(userQueue)
                .to(b2gExchange)
                .with("user.#");
    }

    @Bean
    public Binding reviewCreatedBinding(TopicExchange b2gExchange, Queue reviewQueue) {
        return BindingBuilder.bind(reviewQueue)
                .to(b2gExchange)
                .with("review.created");
    }

    @Bean
    public Binding reviewUpdatedBinding(TopicExchange b2gExchange, Queue reviewQueue) {
        return BindingBuilder.bind(reviewQueue)
                .to(b2gExchange)
                .with("review.updated");
    }

    @Bean
    public Binding reviewDeletedBinding(TopicExchange b2gExchange, Queue reviewQueue) {
        return BindingBuilder.bind(reviewQueue)
                .to(b2gExchange)
                .with("review.deleted");
    }
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

}
