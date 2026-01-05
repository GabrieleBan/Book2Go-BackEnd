package com.b2g.commons;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookFormatCreatedEvent {
    public UUID formatId;
    public UUID bookId;
    public String formatType;
    public boolean isPhisical;
}