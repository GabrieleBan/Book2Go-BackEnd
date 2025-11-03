package com.b2g.recomendationservice.model.relationships;
import com.b2g.recomendationservice.model.nodes.Publisher;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.Date;

@RelationshipProperties
public class PublishedBy {
    @Id
    @GeneratedValue
    Long id;

    @TargetNode
    private Publisher publisher;
    private Date publicationDate;
}
