package com.b2g.recomendationservice.model.nodes;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Builder
@Node
@Getter
@Data
public class Writer {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;
    private String name;
}
