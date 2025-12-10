package com.b2g.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
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

    @Value("${app.rabbitmq.routing-key.signup-ticket}")
    private String userRegisteredRoutingKey;

    @Value("${app.rabbitmq.queue.user-signup}")
    private String userRegistrationQueue;
    @Value("${app.rabbitmq.queue.name.suffix}")
    private String queueNameSuffix;

    @Value("${app.rabbitmq.binding-key.lend.created}")
    private String lendCreated;
    @Value("${app.rabbitmq.binding-key.lend.failed}")
    private String lendFailed;
    @Value("${app.rabbitmq.binding-key.lend.requested.paymentcheck}")
    private String lendRequestedPaymentCheck;
    @Value("${app.rabbitmq.binding-key.lend.requested.addresscheck}")
    private String lendRequestedAddressCheck;
    @Value("${app.rabbitmq.binding-key.lend.request.created}")
    private String lendRequestCreated;

    @Value("${app.rabbitmq.binding-key.lend.ready}")
    private String lendReadyForRetrieval;

    @Value("${app.rabbitmq.binding-key.lend.failed}")
    private String lendEnded;

    @Bean
    public Exchange bookToGoExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue userQueue() {
        return new Queue(userRegistrationQueue, true);
    }
    @Bean
    public Binding userCreatedBinding(Queue userQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(userQueue)
                .to(bookToGoExchange)
                .with(userRegisteredRoutingKey)
                .noargs();
    }

    @Bean
    Queue lendQueue() {
        String queueName = queueNameSuffix+".lend.queue";
        return new Queue(queueName, true);
    }

    @Bean
    public Binding lendCreatedBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendCreated)
                .noargs();
    }

    @Bean
    public Binding lendFailedBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendFailed)
                .noargs();
    }

    @Bean
    public Binding lendRequestedPaymentCheckBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendRequestedPaymentCheck)
                .noargs();
    }

    @Bean
    public Binding lendRequestCreatedPaymentCheckBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendRequestCreated)
                .noargs();
    }

    @Bean
    public Binding lendRequestedAddressCheckBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendRequestedAddressCheck)
                .noargs();
    }

    @Bean
    public Binding lendReadyForRetrievalBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendReadyForRetrieval)
                .noargs();
    }

    @Bean
    public Binding lendEndedBinding(Queue lendQueue, Exchange bookToGoExchange) {
        return BindingBuilder.bind(lendQueue)
                .to(bookToGoExchange)
                .with(lendEnded)
                .noargs();
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
