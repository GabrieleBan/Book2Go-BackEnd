package com.b2g.readerservice.model;
import com.b2g.commons.SubscriptionType;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscription;
    @Embedded
    private Address address;
    private String description;
    @PrePersist
    private void prePersist() {
        if (subscription == null) {
            subscription = SubscriptionType.UNSUBSCRIBED;
        }
    }
}
