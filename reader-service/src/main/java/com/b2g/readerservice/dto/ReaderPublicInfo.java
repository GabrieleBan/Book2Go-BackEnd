package com.b2g.readerservice.dto;

import com.b2g.readerservice.model.Reader;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class ReaderPublicInfo {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;
    private String imageUrl;
    private String name;
    private String surname;
    private String description;

    public static ReaderPublicInfo fromReader(Reader reader) {
        return ReaderPublicInfo.builder().
                id(reader.getId())
                .imageUrl(reader.getImageUrl())
                .name(reader.getName())
                .description(reader.getDescription())
                .username(reader.getUsername())
                .build();
    }
}
