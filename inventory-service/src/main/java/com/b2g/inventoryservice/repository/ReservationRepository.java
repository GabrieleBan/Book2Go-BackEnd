package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.Reservation;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.model.valueObjects.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation findByCopyIdAndState(CopyId copyId, ReservationState state);
}
