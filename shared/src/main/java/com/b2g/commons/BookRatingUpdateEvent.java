package com.b2g.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookRatingUpdateEvent {
    public UUID bookId;
    public float rating;
    public int totalReviews;
}
