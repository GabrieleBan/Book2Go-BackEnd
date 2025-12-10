package com.b2g.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LendingMessage {
    private UUID userId;
    private UUID bookId;
    private UUID formatId;
    private Integer physicalId;
    private UUID lendId;
    private FormatType formatType;
    private String message;
    private LendState lendState;
    private PaymentMethod paymentMethod;
    private UUID libraryId;
    private LocalDate startDate;
    private LocalDate endDate;
}
