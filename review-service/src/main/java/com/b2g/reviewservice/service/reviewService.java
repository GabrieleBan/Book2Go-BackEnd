package com.b2g.reviewservice.service;

import com.b2g.reviewservice.dto.ReviewDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class ReviewService {
    @Value("${app.rabbitmq.exchange}")
    private  String topicExchange;
    private final RabbitTemplate rabbitTemplate;

    public ReviewService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendReview(ReviewDTO review) {
        rabbitTemplate.convertAndSend(topicExchange, "review.created", review);
    }
}