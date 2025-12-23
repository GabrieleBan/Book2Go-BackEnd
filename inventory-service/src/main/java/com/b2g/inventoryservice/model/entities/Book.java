package com.b2g.inventoryservice.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class Book {

    @Id
    @Column(name = "book_id",unique = true, nullable = false)
    private UUID bookId;


    protected Book() {}

    public Book(UUID bookId) {
        this.bookId = bookId;
    }
}