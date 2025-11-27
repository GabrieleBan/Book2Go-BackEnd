package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.RentalBookFormat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RentalBookFormatRepository extends JpaRepository<RentalBookFormat, UUID> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RentalBookFormat r WHERE r.formatId = :formatId")
    RentalBookFormat lockFormat(@Param("formatId") UUID formatId);
}
