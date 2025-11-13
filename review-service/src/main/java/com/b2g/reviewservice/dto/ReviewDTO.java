package com.b2g.reviewservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
public class ReviewDTO {
    Long id;
    UUID readerId;
    UUID bookId;
    float rating;
}