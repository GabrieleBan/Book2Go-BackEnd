package com.b2g.recomendationservice.repository;


import com.b2g.recomendationservice.dto.ReviewDTO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends Neo4jRepository<ReviewDTO, Long> {
    // Tutte le recensioni di un libro
    @Query("""
        MATCH (r:Reader)-[rel:REVIEWS]->(b:Book {id:$bookId})
        RETURN r.id AS readerId, b.id AS bookId, rel.rating AS rating
    """)
    List<ReviewDTO> findAllBookReviews(@Param("bookId") UUID bookId);

    // Tutte le recensioni scritte da un lettore
    @Query("""
        MATCH (r:Reader {id:$readerId})-[rel:REVIEWS]->(b:Book)
        RETURN r.id AS readerId, b.id AS bookId, rel.rating AS rating
    """)
    List<ReviewDTO> findAllReviewsWrittenByReader(@Param("readerId") UUID readerId);

    // Crea o modifica una recensione
    @Query("""
        MATCH (r:Reader {id:$readerId}), (b:Book {id:$bookId})
        MERGE (r)-[rel:REVIEWS]->(b)
        SET rel.rating = $rating
        RETURN r.id AS readerId, b.id AS bookId, rel.rating AS rating
    """)
    ReviewDTO createReview(@Param("readerId") UUID readerId,
                           @Param("bookId") UUID bookId,
                           @Param("rating") float rating);

    // Modifica una recensione (usa createReview)
    default ReviewDTO modifyReview(UUID readerId, UUID bookId, float rating) {
        return createReview(readerId, bookId, rating);
    }

    // Cancella una recensione e ritorna il numero di relazioni eliminate
    @Query("""
        MATCH (r:Reader {id:$readerId})-[rel:REVIEWS]->(b:Book {id:$bookId})
        WITH rel, count(rel) AS deletedCount
        DELETE rel
        RETURN deletedCount
    """)
    int deleteReview(@Param("readerId") UUID readerId,
                     @Param("bookId") UUID bookId);
}
