package com.b2g.reviewservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Date;
import java.util.UUID;

@Data
@Entity
public class Review {
    @Id
    private UUID id;
    private UUID authorId;
    private UUID bookId;
    private float overallScore;
    private String comment;
    private Date postedDate;
    private boolean canBeShown;
}
