package com.b2g.inventoryservice.config;
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

    @Value("${app.rabbitmq.bindingkey.lend.request.created}")
    private String lendRequestedAtLibraryKey;

    @Value("${app.rabbit.bindingkey.book.format.created}")
    private String formatCreatedInCatalogKey;


    @Bean
    public Exchange b2gExchange() {
        return new TopicExchange(exchangeName, true, false);
    }


    @Bean
    Queue inventoryQueue() {
        return new Queue("inventory.lend.queue", true);
    }
    @Bean
    public Binding userCreatedBinding(Queue inventoryQueue, Exchange b2gExchange) {
        return BindingBuilder.bind(inventoryQueue)
                .to(b2gExchange)
                .with(lendRequestedAtLibraryKey)
                .noargs();
    }
    @Bean
    Queue inventoryBookFormatQueue() {
        return new Queue("inventory.catalog.queue", true);
    }
    @Bean
    public Binding bookFormatCreatedBinding(Queue inventoryBookFormatQueue, Exchange b2gExchange) {
        return BindingBuilder.bind(inventoryBookFormatQueue)
                .to(b2gExchange)
                .with(formatCreatedInCatalogKey)
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