package com.b2g.catalogservice.model.VO;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private float discountPercent;

    public BigDecimal finalPrice() {
        return purchasePrice.multiply(
                BigDecimal.valueOf(1 - discountPercent / 100.0)
        );
    }

    public boolean isInRange(BigDecimal min, BigDecimal max) {
        BigDecimal finalPrice = finalPrice();

        if (min != null && finalPrice.compareTo(min) < 0) {
            return false;
        }
        if (max != null && finalPrice.compareTo(max) > 0) {
            return false;
        }
        return true;
    }
}