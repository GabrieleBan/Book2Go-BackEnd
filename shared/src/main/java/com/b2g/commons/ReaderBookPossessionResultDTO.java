package com.b2g.commons;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;
@Setter
@Data
@Getter
@NoArgsConstructor
public class ReaderBookPossessionResultDTO {

    private Long reviewId;
    private UUID userId;
    private UUID bookId;
    private LocalDate startDate;
}