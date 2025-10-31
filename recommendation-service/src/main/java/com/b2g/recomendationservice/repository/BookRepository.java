package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Book;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.UUID;

public interface BookRepository  extends Neo4jRepository<Book, UUID> {
}
