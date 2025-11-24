package com.b2g.readerservice.dto;

import com.b2g.readerservice.model.Reader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@NoArgsConstructor
@Builder
@AllArgsConstructor
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
