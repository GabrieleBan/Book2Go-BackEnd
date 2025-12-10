package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
@Repository
public interface BookRepository  extends Neo4jRepository<Book, String> {
    @Query(
            value = """
        MATCH (b:Book)-[:Tag]->(t:Tag)
        WHERE t.id IN $tagIds
        WITH b, count(DISTINCT t) AS matchedTags
        RETURN DISTINCT b, matchedTags
        ORDER BY matchedTags DESC, b.title ASC
        """,
            countQuery = """
        MATCH (b:Book)-[:Tag]->(t:Tag)
        WHERE t.id IN $tagIds
        RETURN count(DISTINCT b)
        """
    )
    Page<Book> findByTagsIdIn(@Param("tagIds") Set<String> tagIds, Pageable pageable);
    @Query(
            value = """
        MATCH (b:Book)-[:Tag]->(t:Tag)
        WHERE t.id IN $tagIds
        WITH b, collect(DISTINCT t.id) AS matchedTags
        WHERE size(matchedTags) = $tagCount
        RETURN b
        """,
            countQuery = """
        MATCH (b:Book)-[:Tag]->(t:Tag)
        WHERE t.id IN $tagIds
        WITH b, collect(DISTINCT t.id) AS matchedTags
        WHERE size(matchedTags) = $tagCount
        RETURN count(DISTINCT b)
        """
    )
    Page<Book> findByAllTags(@Param("tagIds") Set<String> tagIds,
                             @Param("tagCount") long tagCount,
                             Pageable pageable);

    // Raccomandazione per autore o publisher con paginazione
    @Query(
            value = """
            MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)
            OPTIONAL MATCH (b)-[:WRITTEN_BY]->(:Writer)<-[:WRITTEN_BY]-(rec:Book)
            OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(:Publisher)<-[:PUBLISHED_BY]-(pub:Book)
            WITH collect(DISTINCT rec) + collect(DISTINCT pub) AS candidateBooks
            UNWIND candidateBooks AS c
            OPTIONAL MATCH (c)<-[:REVIEWS]-(r:Reader)
            RETURN c, count(r) AS reviewCount
            ORDER BY reviewCount DESC
            """,
            countQuery = """
            MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)
            OPTIONAL MATCH (b)-[:WRITTEN_BY]->(:Writer)<-[:WRITTEN_BY]-(rec:Book)
            OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(:Publisher)<-[:PUBLISHED_BY]-(pub:Book)
            WITH collect(DISTINCT rec) + collect(DISTINCT pub) AS candidateBooks
            UNWIND candidateBooks AS c
            RETURN count(DISTINCT c)
            """
    )
    Page<Book> recommendByAuthorOrPublisher(@Param("userId") String userId, Pageable pageable);


    // Raccomandazione lettori simili con paginazione
    @Query(
            value = """
            MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)<-[:REVIEWS]-(other:Reader)-[:REVIEWS]->(rec:Book)
            WHERE NOT (u)-[:REVIEWS]->(rec)
            RETURN rec, count(other) AS score
            ORDER BY score DESC
            """,
            countQuery = """
            MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)<-[:REVIEWS]-(other:Reader)-[:REVIEWS]->(rec:Book)
            WHERE NOT (u)-[:REVIEWS]->(rec)
            RETURN count(DISTINCT rec)
            """
    )
    Page<Book> recommendBySimilarReaders(@Param("userId") String userId, Pageable pageable);
    @Query("""
    MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)
    OPTIONAL MATCH (b)-[:WRITTEN_BY]->(:Writer)<-[:WRITTEN_BY]-(rec:Book)
    OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(:Publisher)<-[:PUBLISHED_BY]-(pub:Book)
    WITH collect(DISTINCT rec) + collect(DISTINCT pub) AS candidateBooks
    UNWIND candidateBooks AS c
    OPTIONAL MATCH (c)<-[:REVIEWS]-(r:Reader)
    RETURN c, count(r) AS reviewCount
    ORDER BY reviewCount DESC
    """)
    List<Book> recommendByAuthorOrPublisher(@Param("userId") String userId);

    @Query("""
    MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)<-[:REVIEWS]-(other:Reader)-[:REVIEWS]->(rec:Book)
    WHERE NOT (u)-[:REVIEWS]->(rec)
    RETURN rec, count(other) AS score
    ORDER BY score DESC
    """)
    List<Book> recommendBySimilarReaders(@Param("userId") String userId);
}

//
//@Query(
//        value = """
//        MATCH (b:Book)-[:Tag]->(t:Tag)
//        WHERE t.id IN $tagIds
//        OPTIONAL MATCH (b)-[:WRITTEN_BY]->(a:Writer)
//        OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(p:Publisher)
//        OPTIONAL MATCH (b)-[:Tag]->(tags:Tag)
//        WITH b, collect(DISTINCT a) AS author, collect(DISTINCT tags) AS tags, p AS publisher, count(DISTINCT t) AS matchedTags
//        RETURN b { .id, .title, author: author, publisher: publisher, tags: tags }, matchedTags
//        ORDER BY matchedTags DESC, b.title ASC
//        """,
//        countQuery = """
//        MATCH (b:Book)-[:Tag]->(t:Tag)
//        WHERE t.id IN $tagIds
//        RETURN count(DISTINCT b)
//        """
//)
//Page<Book> findByTagsIdIn(@Param("tagIds") Set<String> tagIds, Pageable pageable);
//2️⃣ findByAllTags – libri che contengono tutti i tag passati
//        java
//Copia codice
//@Query(
//        value = """
//        MATCH (b:Book)-[:Tag]->(t:Tag)
//        WHERE t.id IN $tagIds
//        WITH b, collect(DISTINCT t.id) AS matchedTags
//        WHERE size(matchedTags) = $tagCount
//        OPTIONAL MATCH (b)-[:WRITTEN_BY]->(a:Writer)
//        OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(p:Publisher)
//        OPTIONAL MATCH (b)-[:Tag]->(tags:Tag)
//        RETURN b { .id, .title, author: collect(DISTINCT a), publisher: p, tags: collect(DISTINCT tags) }
//        """,
//        countQuery = """
//        MATCH (b:Book)-[:Tag]->(t:Tag)
//        WHERE t.id IN $tagIds
//        WITH b, collect(DISTINCT t.id) AS matchedTags
//        WHERE size(matchedTags) = $tagCount
//        RETURN count(DISTINCT b)
//        """
//)
//Page<Book> findByAllTags(@Param("tagIds") Set<String> tagIds,
//                         @Param("tagCount") long tagCount,
//                         Pageable pageable);
//3️⃣ recommendByAuthorOrPublisher – raccomandazioni basate su autore o publisher
//java
//Copia codice
//@Query(
//        value = """
//        MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)
//        OPTIONAL MATCH (b)-[:WRITTEN_BY]->(:Writer)<-[:WRITTEN_BY]-(rec:Book)
//        OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(:Publisher)<-[:PUBLISHED_BY]-(pub:Book)
//        WITH collect(DISTINCT rec) + collect(DISTINCT pub) AS candidateBooks
//        UNWIND candidateBooks AS c
//        OPTIONAL MATCH (c)-[:WRITTEN_BY]->(a:Writer)
//        OPTIONAL MATCH (c)-[:PUBLISHED_BY]->(p:Publisher)
//        OPTIONAL MATCH (c)-[:Tag]->(tags:Tag)
//        OPTIONAL MATCH (c)<-[:REVIEWS]-(r:Reader)
//        RETURN c { .id, .title, author: collect(DISTINCT a), publisher: p, tags: collect(DISTINCT tags) }, count(r) AS reviewCount
//        ORDER BY reviewCount DESC
//        """,
//        countQuery = """
//        MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)
//        OPTIONAL MATCH (b)-[:WRITTEN_BY]->(:Writer)<-[:WRITTEN_BY]-(rec:Book)
//        OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(:Publisher)<-[:PUBLISHED_BY]-(pub:Book)
//        WITH collect(DISTINCT rec) + collect(DISTINCT pub) AS candidateBooks
//        UNWIND candidateBooks AS c
//        RETURN count(DISTINCT c)
//        """
//)
//Page<Book> recommendByAuthorOrPublisher(@Param("userId") String userId, Pageable pageable);
//4️⃣ recommendBySimilarReaders – libri letti da lettori simili
//        java
//Copia codice
//@Query(
//        value = """
//        MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)<-[:REVIEWS]-(other:Reader)-[:REVIEWS]->(rec:Book)
//        WHERE NOT (u)-[:REVIEWS]->(rec)
//        OPTIONAL MATCH (rec)-[:WRITTEN_BY]->(a:Writer)
//        OPTIONAL MATCH (rec)-[:PUBLISHED_BY]->(p:Publisher)
//        OPTIONAL MATCH (rec)-[:Tag]->(tags:Tag)
//        RETURN rec { .id, .title, author: collect(DISTINCT a), publisher: p, tags: collect(DISTINCT tags) }, count(other) AS score
//        ORDER BY score DESC
//        """,
//        countQuery = """
//        MATCH (u:Reader {id: $userId})-[:REVIEWS]->(b:Book)<-[:REVIEWS]-(other:Reader)-[:REVIEWS]->(rec:Book)
//        WHERE NOT (u)-[:REVIEWS]->(rec)
//        RETURN count(DISTINCT rec)
//        """
//)
//Page<Book> recommendBySimilarReaders(@Param("userId") String userId, Pageable pageable);