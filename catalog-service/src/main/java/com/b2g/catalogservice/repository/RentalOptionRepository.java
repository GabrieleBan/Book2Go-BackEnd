package com.b2g.catalogservice.repository;

import com.b2g.catalogservice.model.RentalOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RentalOptionRepository extends JpaRepository<RentalOption, UUID> {
}
