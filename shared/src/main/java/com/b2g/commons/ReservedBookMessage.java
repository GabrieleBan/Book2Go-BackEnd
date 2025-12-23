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
public class ReservedBookMessage {
    public UUID bookId;
    public Integer copyNumber;
    public UUID libraryId;
}
