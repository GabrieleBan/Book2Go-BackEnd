package com.b2g.commons;

import lombok.*;

import java.sql.Date;
import java.util.UUID;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
public class ReviewConfirmationDTO {
//    usare questo oggetto per leggere risposta
    private Long reviewId;
    private UUID userId;
    private UUID bookId;
    private Date pubblicationDate;
    @Setter
    private boolean confirmed;
    private String reason;
}
