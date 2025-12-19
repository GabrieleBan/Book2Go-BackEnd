package com.b2g.inventoryservice.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class Book {

    @Id
    private UUID bookId;

    protected Book() {}

    public Book(UUID bookId) {
        this.bookId = bookId;
    }
}