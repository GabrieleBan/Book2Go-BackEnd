package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.ReservationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRequestRepository extends JpaRepository<ReservationRequest, Long> {
}
