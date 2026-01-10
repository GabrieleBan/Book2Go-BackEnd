package com.b2g.reviewservice.repository;

import com.b2g.reviewservice.model.Review;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findReviewsByBookId(UUID bookId);

    Review findByReviewerIdAndBookId(UUID reviewerId, UUID bookId);


    @Query("SELECT r FROM Review r " +
            "WHERE r.bookId = :bookId AND r.canBeShown = :canBeShown " +
            "ORDER BY r.postedDate DESC")
    Page<Review> findReviewsByBookIdAndCanBeShown(@Param("bookId") UUID bookId,
                                                  @Param("canBeShown") boolean canBeShown,
                                                  Pageable pageable);
}
