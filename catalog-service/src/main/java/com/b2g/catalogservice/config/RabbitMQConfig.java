package com.b2g.catalogservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.queue.name}")
    private String catalogQueueName;

    @Value("${app.rabbitmq.bindinkey.availability.all}")
    private String availabilityAllKey;

    @Bean
    public Exchange catalogExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue catalogAvailabilityQueue() {
        return new Queue(catalogQueueName + ".availability.queue", true);
    }

    @Bean
    public Binding catalogAvailabilityBinding(
            Queue catalogAvailabilityQueue,
            Exchange catalogExchange
    ) {
        return BindingBuilder
                .bind(catalogAvailabilityQueue)
                .to(catalogExchange)
                .with(availabilityAllKey)
                .noargs();
    }


    @Bean
    public Exchange b2gExchange() {
        return new TopicExchange(exchangeName, true, false);
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

    @Bean(name = "safeRabbitTemplate")
    public RabbitTemplate safeRabbitTemplate(ConnectionFactory connectionFactory,
                                             MessageConverter jsonMessageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        template.setMandatory(true); // necessario per intercettare messaggi non routati

        template.setReturnsCallback(returned -> {
            throw new RuntimeException("Messaggio NON ROUTATO: " + returned.getReplyText());
        });

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                throw new RuntimeException("Broker NON ha confermato il messaggio: " + cause);
            }
        });

        return template;
    }
}
