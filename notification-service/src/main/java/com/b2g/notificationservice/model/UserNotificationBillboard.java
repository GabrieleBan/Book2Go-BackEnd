package com.b2g.notificationservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserNotificationBillboard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID note_id;
    private UUID userid;
    private String title;
    private String description;
    private LocalDateTime timestamp;

}
