package com.b2g.inventoryservice.model.entities;


import com.b2g.inventoryservice.exceptions.StockException;
import com.b2g.inventoryservice.model.valueObjects.StockId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
@Entity
@Getter
public class RetailStock {

    @EmbeddedId
    private StockId id;

    private int quantity;

    protected RetailStock() {}

    private RetailStock(StockId id, int quantity) {
        this.id = id;
        this.setInitialQuantintity(quantity);
    }

    private void setInitialQuantintity(int quantity) {
        if (quantity <= 0) {
            throw new StockException("Lo stock non può avere valore negativo");
        }
        this.quantity = quantity;
    }

    public static RetailStock initializeStock(ReferenceBook book, Library lib, int quantity) {
        return new RetailStock(new StockId(book.getBookId(),lib.getLibrary_id()), quantity);
    }

    public int increase(int qty) {
        if (qty < 0) throw new StockException("Cannot increase quantity less than zero");
        quantity += qty;
        return quantity;
    }

    public int decrease(int qty) {
        if (qty < 0) throw new StockException("Cannot decrease quantity less than zero");
        if (quantity <= 0) {
            throw new StockException("No stock available in this library");
        }
        if (quantity-qty < 0) {throw new StockException("Cannot decrese below zero");}
        quantity-=qty;

        return quantity;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }
}
