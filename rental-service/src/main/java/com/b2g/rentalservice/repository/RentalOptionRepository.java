package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.RentalOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalOptionRepository extends JpaRepository<RentalOption, UUID> {
    Optional<RentalOption> findByDurationDaysAndPriceAndDescription(Integer durationDays, BigDecimal price, String description);
}
