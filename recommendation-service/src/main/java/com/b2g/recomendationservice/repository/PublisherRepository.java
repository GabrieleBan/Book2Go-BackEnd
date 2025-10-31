package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Publisher;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.UUID;

public interface PublisherRepository extends Neo4jRepository<Publisher, UUID> {
}
