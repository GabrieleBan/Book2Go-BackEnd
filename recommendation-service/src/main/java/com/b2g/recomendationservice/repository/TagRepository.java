package com.b2g.recomendationservice.repository;

import com.b2g.recomendationservice.model.nodes.Tag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends Neo4jRepository<Tag, String> {
}
