package com.b2g.recomendationservice.model.relationships;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
public class Reviews {
    @Id
    @GeneratedValue
    private Long id;
    @Getter
    @Setter
    private float rating;
}
