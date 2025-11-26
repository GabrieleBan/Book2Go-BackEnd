package com.b2g.recomendationservice.model.nodes;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Getter
@Setter
@Node
public class Publisher {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;
    private String name;
}
