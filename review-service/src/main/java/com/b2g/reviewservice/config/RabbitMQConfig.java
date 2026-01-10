package com.b2g.reviewservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;

    @Value("${app.rabbitmq.binding-key.review.authorized}")
    private String reviewAuthorizedBindingKey;

//    @Value("${app.rabbitmq.binding-key.review.rejected}")
//    private String reviewRejectedBindingKey;

//    @Value("${app.rabbitmq.routing-key.review.created}")
//    private String reviewCreatedRoutingKey;


    @Bean
    public TopicExchange b2gExchange() {
        return new TopicExchange(exchangeName, true, false);
    }


    @Bean
    public Queue reviewAuthorizationQueue() {
        return new Queue("review.authorization.queue", true, false, false);
    }


    @Bean
    public Binding reviewConfirmedBinding(TopicExchange b2gExchange, Queue reviewAuthorizationQueue) {
        return BindingBuilder.bind(reviewAuthorizationQueue)
                .to(b2gExchange)
                .with(reviewAuthorizedBindingKey);
    }

//    @Bean
//    public Binding reviewRejectionBinding(TopicExchange b2gExchange, Queue reviewAuthorizationQueue) {
//        return BindingBuilder.bind(reviewAuthorizationQueue)
//                .to(b2gExchange)
//                .with(reviewRejectedBindingKey);
//    }


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
