package com.b2g.rentalservice.repository;

import com.b2g.rentalservice.model.Lending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2g.commons.LendState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LendingHistoryRepository extends JpaRepository<Lending, UUID> {
    Optional<Lending> findFirstByUserIdAndStateIn(UUID userId, List<LendState> states);
    boolean existsByUserIdAndStateIn(UUID userId, List<LendState> states);

    Optional<Lending> findFirstByUserIdAndFormatIdAndStateIn(UUID userId, UUID formatId, Collection<LendState> states);

    Optional<Lending> findFirstByUserIdAndFormatIdAndPhysBookIdAndStateIn(UUID userId, UUID formatId, Integer physBookId, Collection<LendState> states);

    List<Lending> findByUserIdAndState(UUID userId, LendState state);
}
