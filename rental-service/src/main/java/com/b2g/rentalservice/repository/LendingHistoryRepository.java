package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.LendingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LendingHistoryRepository extends JpaRepository<LendingHistory, UUID> {
}
