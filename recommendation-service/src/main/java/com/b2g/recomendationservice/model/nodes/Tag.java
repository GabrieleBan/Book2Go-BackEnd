package com.b2g.recomendationservice.model.nodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;
@Node
@AllArgsConstructor
@Getter
public class Tag {
    @Id
    private UUID id;
    private String name;
}
