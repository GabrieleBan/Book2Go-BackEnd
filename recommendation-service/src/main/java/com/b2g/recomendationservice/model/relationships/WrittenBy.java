package com.b2g.recomendationservice.model.relationships;

import com.b2g.recomendationservice.model.nodes.Writer;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class WrittenBy {

    @Id
    @GeneratedValue
    Long id;
    @Setter
    @TargetNode
    private Writer writer;

}
