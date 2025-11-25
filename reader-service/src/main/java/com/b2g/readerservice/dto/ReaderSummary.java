package com.b2g.readerservice.dto;

import com.b2g.readerservice.model.Reader;
import lombok.*;

import java.util.UUID;
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@Setter
public class ReaderSummary {
    private UUID id;
    private String username;
    private String email;
    private String imageUrl;


    public static ReaderSummary fromReader(Reader reader) {
        return ReaderSummary.builder()
                .id(reader.getId())
                .username(reader.getUsername())
                .email(reader.getEmail())
                .imageUrl(reader.getImageUrl())
                .build();
    }
}
