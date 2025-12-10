package com.b2g.readerservice.config;

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

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.routing-key.user.confirmed}")
    private String userConfirmedRoutingKey;
    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;
    @Value("${app.rabbitmq.binding-key.lend.created}")
    private String lendCreatedBindingKey;
    @Value("${app.rabbitmq.binding-key.lend.ended}")
    private String lendEndedBindingKey;
    @Value("${app.rabbitmq.binding-key.review.authrequested}")
    private String reviewAuthRequestedBindingKey;




    @Bean
    public TopicExchange b2gExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue userQueue() {
//        per ottenere i dati degli utenti confermati
        String queueName=servicePrefix+ ".user.queue";
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public Binding userBinding(TopicExchange b2gExchange, Queue userQueue) {
        return BindingBuilder.bind(userQueue)
                .to(b2gExchange)
                .with(userConfirmedRoutingKey);
    }

    @Bean
    public Queue lendQueue() {
        String queueName=servicePrefix+ ".lend.queue";
        return new Queue(queueName, true, false, false);
    }
    @Bean
    public Binding lendCreatedBinding(TopicExchange b2gExchange, Queue lendQueue) {
        return BindingBuilder.bind(lendQueue)
                .to(b2gExchange)
                .with(lendCreatedBindingKey);
    }
    @Bean
    public Binding lendExpiredBinding(TopicExchange b2gExchange, Queue lendQueue) {
        return BindingBuilder.bind(lendQueue)
                .to(b2gExchange)
                .with(lendEndedBindingKey);
    }


    @Bean
    public Queue reviewAuthQueue() {
//        per ottenere i dati degli utenti confermati
        String queueName=servicePrefix+ ".review.authorization.queue";
        return new Queue(queueName, true, false, false);
    }
    @Bean
    public Binding reviewAuthorizationBinding(TopicExchange b2gExchange, Queue reviewAuthQueue) {
        return BindingBuilder.bind(reviewAuthQueue)
                .to(b2gExchange)
                .with(reviewAuthRequestedBindingKey);
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
