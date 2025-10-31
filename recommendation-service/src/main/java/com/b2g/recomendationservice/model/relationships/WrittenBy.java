package com.b2g.recomendationservice.model.relationships;

import com.b2g.recomendationservice.model.nodes.Writer;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class WrittenBy {

    @Id
    @GeneratedValue
    Long id;
    @TargetNode
    private Writer writer;

}
