package com.b2g.recomendationservice.model.nodes;

import com.b2g.recomendationservice.model.relationships.PublishedBy;
import com.b2g.recomendationservice.model.relationships.WrittenBy;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Node
@ToString
public class Book {
    @Id
    private UUID id;
    private String title;
    @Relationship(type = "WRITTEN_BY",  direction = Relationship.Direction.OUTGOING)
    private List<WrittenBy> authors;
    @Relationship(type = "PUBLISHED_BY", direction = Relationship.Direction.OUTGOING)
    private PublishedBy publisher;



}
