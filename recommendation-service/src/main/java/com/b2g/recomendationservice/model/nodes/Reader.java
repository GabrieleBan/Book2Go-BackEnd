package com.b2g.recomendationservice.model.nodes;

import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Setter
@Node
public class Reader {
    @Id
    private UUID id;
}
