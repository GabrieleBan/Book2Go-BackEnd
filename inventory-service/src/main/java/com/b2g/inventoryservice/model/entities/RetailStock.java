package com.b2g.inventoryservice.model.entities;


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

    public RetailStock(StockId id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public void increase(int qty) {
        if (qty <= 0) throw new IllegalArgumentException();
        quantity += qty;
    }

    public void decrease() {
        if (quantity <= 0) {
            throw new IllegalStateException("No stock available");
        }
        quantity--;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }
}
