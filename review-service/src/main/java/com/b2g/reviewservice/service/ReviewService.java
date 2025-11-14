package com.b2g.reviewservice.service;

import com.b2g.reviewservice.dto.RequestCreateReviewDTO;
import com.b2g.reviewservice.dto.ReviewConfirmationDTO;
import com.b2g.reviewservice.dto.ReviewDTO;
import com.b2g.reviewservice.model.Review;
import com.b2g.reviewservice.repository.ReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    @Value("${app.rabbitmq.exchange}")
    private  String topicExchange;
    private final RabbitTemplate rabbitTemplate;
    private final ReviewRepository reviewRepository;



    public List<Review> getAllBookReviews(UUID bookId) {
        return reviewRepository.findReviewsByBookIdAndCanBeShownOrdered(bookId,true);
    }

    public boolean createBookReview(@Valid RequestCreateReviewDTO reviewDTO, UUID reviewerId) {
        float overall = reviewDTO.overallScore();
        String action;

        if (overall < 0 || overall > 5) {
            throw new IllegalArgumentException("overall score must be between 0 and 5");
        }

        Review existingReview = reviewRepository.findByReviewerIdAndBookId(reviewerId, reviewDTO.bookId());
        Review review=null;
        if (existingReview == null) {
            // Nuova recensione
            review = Review.builder()
                    .bookId(reviewDTO.bookId())
                    .reviewerId(reviewerId)
                    .canBeShown(false)
                    .overallScore(overall)
                    .text(reviewDTO.text())
                    .title(reviewDTO.title())
                    .postedDate(new Date(System.currentTimeMillis())) // data attuale
                    .build();
            reviewRepository.save(review);
            action = "requiresConfirmation";
        } else {
            // Aggiorna recensione esistente
            existingReview.setOverallScore(overall);
            existingReview.setText(reviewDTO.text());
            existingReview.setTitle(reviewDTO.title());
            existingReview.setCanBeShown(false);
            existingReview.setPostedDate(new Date(System.currentTimeMillis())); // aggiorna la data , ha senso?
            reviewRepository.save(existingReview);
            review = existingReview;
            action = "updated";


        }
        notifyReviewAction(review,action);

        return true;
    }

    private void notifyReviewAction(Review review, String action) {
        String routingKey = switch (action) {
            case "created" -> "review.created";
            case "updated" -> "review.updated";
            case "deleted" -> "review.deleted";
            case "requiresConfirmation" -> "review.awaitsConfirmation";
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
        if(!routingKey.equals("review.awaitsConfirmation")) {
            ReviewDTO reviewDTO = Review.toReviewDTO(review);
            rabbitTemplate.convertAndSend(topicExchange, routingKey, reviewDTO);
        }
        else{
            ReviewConfirmationDTO reviewAwaiting = Review.toReviewConfirmationDTO(review);
            rabbitTemplate.convertAndSend(topicExchange, routingKey, reviewAwaiting);
        }

    }

    @RabbitListener(queues = "review.authorization.queue")
    public void handleReviewEvents(ReviewConfirmationDTO confirmedReview, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        System.out.println("Routing key usata: {}"+ routingKey);
        switch (routingKey) {
            case "review.created" -> {
                System.out.println("Ricevuto messaggio di conferma review: {}"+ confirmedReview);
                confirmReview(confirmedReview);
            }
        }

    }

    private void confirmReview(ReviewConfirmationDTO confirmedReview) {
        reviewRepository.findById(confirmedReview.getReviewId()).ifPresent(review -> {
            if (confirmedReview.isConfirmed()) {
                review.setCanBeShown(true);
                reviewRepository.save(review);
            } else {
                reviewRepository.delete(review);
            }
        });
    }
}