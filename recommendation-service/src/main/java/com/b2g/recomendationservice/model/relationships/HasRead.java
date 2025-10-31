package com.b2g.recomendationservice.model.relationships;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
public class HasRead {
    @Id
    @GeneratedValue
    Long id;
}
