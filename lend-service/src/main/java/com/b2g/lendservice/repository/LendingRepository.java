package com.b2g.lendservice.repository;

import com.b2g.commons.LendState;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.model.Lending;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LendingRepository extends JpaRepository<Lending, UUID> {

    List<Lending> findByUserIdAndStateIn(UUID userId, Collection<LendState> states);


    List<Lending> findByUserIdAndState(UUID userId, LendState lendState);


    Lending findByUserIdAndCopyAndStateIn(UUID userId, LendableCopy copy, Collection<LendState> states);

    List<Lending> findByUserIdAndStateAndLibraryId(UUID userId, LendState state, UUID libraryId);
}
