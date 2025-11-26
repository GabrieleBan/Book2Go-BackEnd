package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Publisher;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface PublisherRepository extends Neo4jRepository<Publisher, String> {
    Optional<Publisher> findByName(String name);
}
