package com.b2g.reviewservice.service;

import com.b2g.commons.ReviewConfirmationDTO;
import com.b2g.reviewservice.dto.RequestCreateReviewDTO;

import com.b2g.reviewservice.dto.ReviewDTO;
import com.b2g.reviewservice.model.Review;
import com.b2g.reviewservice.repository.ReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    @Value("${app.rabbitmq.exchange}")
    private  String topicExchange;
    private final RabbitTemplate rabbitTemplate;
    private final ReviewRepository reviewRepository;



    public Page<Review> getAllBookReviews(UUID bookId, Pageable pageable) {
        return reviewRepository.findReviewsByBookIdAndCanBeShown(bookId, true, pageable);
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
            log.info("created {}",review);
            review=reviewRepository.save(review);
            action = "requiresConfirmation";
        } else {
            // Aggiorna recensione esistente
            existingReview.setOverallScore(overall);
            existingReview.setText(reviewDTO.text());
            existingReview.setTitle(reviewDTO.title());
            existingReview.setPostedDate(new Date(System.currentTimeMillis())); // aggiorna la data , ha senso?
            reviewRepository.save(existingReview);
            review = existingReview;
            if(existingReview.isCanBeShown())
                action = "updated";
            else
                action = "requiresConfirmation";
        }
        notifyReviewAction(review,action);

        return true;
    }
    @Value("${app.rabbitmq.routing-key.review.authorization.requested}")
    private String reviewAuthorizationRequested;
    private void notifyReviewAction(Review review, String action) {
        String routingKey = switch (action) {
            case "created" -> "review.created";
            case "updated" -> "review.updated";
            case "deleted" -> "review.deleted";
            case "requiresConfirmation" -> reviewAuthorizationRequested;
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
        if(!routingKey.equals(reviewAuthorizationRequested)) {
            ReviewDTO reviewDTO = Review.toReviewDTO(review);
            log.info("notifying review modification {}",review);
            rabbitTemplate.convertAndSend(topicExchange, routingKey, reviewDTO);
        }
        else{
            ReviewConfirmationDTO reviewAwaiting = Review.toReviewConfirmationDTO(review);
            log.info("review awaiting {}",reviewAwaiting);
            rabbitTemplate.convertAndSend(topicExchange, routingKey, reviewAwaiting);
        }

    }



    protected void confirmReview(ReviewConfirmationDTO confirmedReview) {
        reviewRepository.findById(confirmedReview.getReviewId()).ifPresent(review -> {
            if (confirmedReview.isConfirmed()) {
                review.setCanBeShown(true);
                reviewRepository.save(review);
                notifyReviewAction(review,"created");
            } else {
                reviewRepository.delete(review);
            }
        });
    }
}