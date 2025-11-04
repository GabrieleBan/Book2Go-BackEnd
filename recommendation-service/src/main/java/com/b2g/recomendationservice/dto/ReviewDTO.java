package com.b2g.recomendationservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
public class ReviewDTO{
    @Id
    @GeneratedValue
    Long id;
    UUID readerId;
    UUID bookId;
    float rating;
}