package com.b2g.readerservice.repository;

import com.b2g.readerservice.model.PersonalLibraryBookItem;
import com.b2g.readerservice.model.PersonalLibraryBookItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReaderLibraryRepository extends JpaRepository<PersonalLibraryBookItem, PersonalLibraryBookItemId> {
    Optional<PersonalLibraryBookItem> findTopByIdUserIdAndIdArchetypeBookIdOrderByExpirationDateDesc(
            UUID userId,
            UUID archetypeBookId
    );
}
