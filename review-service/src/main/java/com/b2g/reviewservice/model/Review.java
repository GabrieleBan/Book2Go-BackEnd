package com.b2g.reviewservice.model;

import com.b2g.reviewservice.dto.ReviewConfirmationDTO;
import com.b2g.reviewservice.dto.ReviewDTO;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"book_id", "reviewer_id"})
        }
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;
    @Column(name = "book_id", nullable = false)
    private UUID bookId;
    private float overallScore;
    private String title;
    private String text;
    private Date postedDate;
    @Setter
    private boolean canBeShown;

    public static ReviewDTO toReviewDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
                .id(review.getId())
                .readerId(review.getReviewerId())
                .bookId(review.getBookId())
                .rating(review.getOverallScore())
                .build();
    }

    public static ReviewConfirmationDTO toReviewConfirmationDTO(Review review) {
        if (review == null) return null;

        return ReviewConfirmationDTO.builder()
                .reviewId(review.getId())
                .userId(review.getReviewerId())
                .bookId(review.getBookId())
                .pubblicationDate(review.getPostedDate())
                .confirmed(review.isCanBeShown()) // confermato
                .reason(null) // da gestire se serve
                .build();
    }

}
