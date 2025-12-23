package com.b2g.inventoryservice.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class Library {
    @Id
    private UUID library_id;

}
