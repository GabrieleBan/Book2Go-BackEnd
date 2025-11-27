package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.PhysicalBookIdentifier;
import com.b2g.rentalservice.model.PhysicalBooks;
import com.b2g.rentalservice.model.RentalBookFormat;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;


@Repository
public interface PhysicalBookRepository extends JpaRepository<PhysicalBooks, PhysicalBookIdentifier> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("""
//        SELECT COALESCE(MAX(pb.id.id), -1)
//        FROM PhysicalBooks pb
//        WHERE pb.id.formatId = :formatId
//    """)
//    int findLastIdForUpdate(@Param("formatId") UUID formatId);


    @Query("SELECT MAX(pb.id.id) FROM PhysicalBooks pb WHERE pb.id.formatId = :formatId")
    Integer findLastId(@Param("formatId") UUID formatId);

}
