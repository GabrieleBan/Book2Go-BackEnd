package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Reader;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ReaderRepository extends Neo4jRepository<Reader, UUID> {
}
