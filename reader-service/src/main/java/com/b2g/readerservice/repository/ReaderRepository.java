package com.b2g.readerservice.repository;

import com.b2g.readerservice.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, UUID> {
    @Query("SELECT r FROM Reader r WHERE r.id IN :readersIds")
    List<Reader> findReadersById(@Param("readersIds")  Set<UUID> readersIds);

    Reader findReaderById(UUID id);
}
