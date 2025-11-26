package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Book;
import com.b2g.recomendationservice.model.nodes.Writer;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface WriterRepository extends Neo4jRepository<Writer, String> {
    @Query("MATCH (w:Writer {id: $writerId})<-[:WRITTEN_BY]-(b:Book) RETURN b")
    public List<Book> findAllWrittenBooksBy(@Param("writerId") String writerId);

    List<Writer> findWriterById(String id);

    Optional<Writer> findByName(String name);
}
