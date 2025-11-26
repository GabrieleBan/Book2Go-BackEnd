package com.b2g.recomendationservice.model.nodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@AllArgsConstructor
@Getter
public class Tag {
    @Id
    private String id;
    private String name;
}
