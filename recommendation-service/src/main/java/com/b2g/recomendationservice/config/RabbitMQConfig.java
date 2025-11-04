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

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("#{'${app.rabbitmq.routing-keys}'.split(',')}")
    private List<String> patternBindingKeys;

    @Value("${app.rabbitmq.queue.name}")
    private String queueName;


    @Bean
    public Exchange b2gExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue recommendationQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public List<Binding> booksBinding(Queue recommendationQueue, Exchange b2gExchange) {
        List<Binding> bindings = new ArrayList<>();
        for (String key : patternBindingKeys)
        {
            bindings.add(BindingBuilder.bind(recommendationQueue)
                    .to(b2gExchange)
                    .with(key)
                    .noargs());
        }
        return bindings;
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
