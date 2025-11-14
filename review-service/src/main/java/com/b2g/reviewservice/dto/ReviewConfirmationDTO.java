package com.b2g.reviewservice.dto;

import lombok.*;

import java.sql.Date;
import java.util.UUID;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReviewConfirmationDTO {
//    usare questo oggetto per leggere risposta
    private Long reviewId;
    private UUID userId;
    private UUID bookId;
    private Date pubblicationDate;
    private boolean confirmed;
    private String reason;
}
