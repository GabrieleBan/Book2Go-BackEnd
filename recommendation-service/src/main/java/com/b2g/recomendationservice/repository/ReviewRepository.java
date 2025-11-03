package com.b2g.recomendationservice.repository;


import com.b2g.recomendationservice.model.relationships.Reviews;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ReviewRepository extends Neo4jRepository<Reviews, Long> {
    @Query("MATCH (r:Reader)-[rel:REVIEWS]->(b:Book {id:$bookId}) RETURN rel")
    List<Reviews> findAllBookReviews(@Param("bookId") UUID bookId);

    @Query("MATCH (r:Reader {id:$readerId})-[rel:REVIEWS]->(b:Book) RETURN rel")
    List<Reviews> findAllReviewsWrittenByReader(@Param("readerId") UUID readerId);

    @Query("""
            MATCH (r:Reader {id:$readerId}), (b:Book {id:$bookId})
            MERGE (r)-[rel:REVIEWS]->(b)
            SET rel.rating = $rating
            RETURN rel""")
    Reviews createReview(@Param("readerId") UUID readerId, @Param("bookId") UUID bookId, @Param("rating") float rating);

    default Reviews modifyReview(UUID readerId, UUID bookId, float rating) {
        return createReview(readerId,bookId,rating);
    }
    @Query("""
            MATCH (r:Reader{id:$readerId})-[rel:REVIEWS]->(b:Book {id:$bookId})
            WITH rel, count(rel) as deletedCount
            DELETE rel
            RETURN deletedCount
            """)
    int deleteReview(@Param("readerId") UUID readerId,@Param("bookId") UUID bookId);
}
