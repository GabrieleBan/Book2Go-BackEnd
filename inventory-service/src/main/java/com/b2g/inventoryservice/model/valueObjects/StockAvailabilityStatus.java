package com.b2g.inventoryservice.model.valueObjects;

public enum StockAvailabilityStatus {
    AVAILABLE, LOW_STOCK, NOT_AVAILABLE,OUT_OF_STOCK;
    public static StockAvailabilityStatus fromTotalQuantity(Integer totalQuantity) {
        if (totalQuantity == null) {
            return NOT_AVAILABLE;
        }
        if (totalQuantity <= 0) {
            return OUT_OF_STOCK;
        }
        if (totalQuantity < 5) {
            return LOW_STOCK;
        }
        return AVAILABLE;
    }
}
