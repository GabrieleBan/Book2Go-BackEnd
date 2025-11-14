package com.b2g.reviewservice.repository;

import com.b2g.reviewservice.model.Review;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findReviewsByBookId(UUID isbn);

    Review findByReviewerIdAndBookId(UUID reviewerId, UUID bookId);

    List<Review> findReviewsByBookIdAndCanBeShown(UUID bookId, boolean canBeShown);
}
