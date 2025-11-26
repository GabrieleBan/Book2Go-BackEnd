package com.b2g.recomendationservice.model.nodes;

import com.b2g.recomendationservice.model.relationships.PublishedBy;
import com.b2g.recomendationservice.model.relationships.WrittenBy;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    private String id;
    private String title;
    @Relationship(type = "WRITTEN_BY",  direction = Relationship.Direction.OUTGOING)
    private List<WrittenBy> authors;
    @Relationship(type = "PUBLISHED_BY", direction = Relationship.Direction.OUTGOING)
    private PublishedBy publisher;
    @Relationship(type="Tag",direction = Relationship.Direction.OUTGOING)
    private List<Tag> tags;



}
