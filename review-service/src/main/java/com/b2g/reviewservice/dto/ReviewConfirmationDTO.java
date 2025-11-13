package com.b2g.reviewservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ReviewConfirmationDTO {
//    usare questo oggetto per leggere risposta
    private UUID reviewId;
    private UUID userId;
    private UUID bookId;
    private Date pubblicationDate;
    private boolean confirmed;
    private String reason;
}
