package com.b2g.readerservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reader {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String imageUrl;
    private String name;
    private String surname;
    private String phone;
    @Embedded
    private Address address;
    private String description;

}
