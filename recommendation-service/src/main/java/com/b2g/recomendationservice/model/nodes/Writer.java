package com.b2g.recomendationservice.model.nodes;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node
public class Writer {
    @Id
    private UUID id;
}
