package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.LendingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2g.commons.LendState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LendingHistoryRepository extends JpaRepository<LendingHistory, UUID> {
    Optional<LendingHistory> findFirstByUserIdAndStateIn(UUID userId, List<LendState> states);
    boolean existsByUserIdAndStateIn(UUID userId, List<LendState> states);

    Optional<LendingHistory> findFirstByUserIdAndFormatIdAndStateIn(UUID userId, UUID formatId, Collection<LendState> states);

    Optional<LendingHistory> findFirstByUserIdAndFormatIdAndPhysBookIdAndStateIn(UUID userId, UUID formatId, Integer physBookId, Collection<LendState> states);

    List<LendingHistory> findByUserIdAndState(UUID userId, LendState state);
}
