package com.b2g.lendservice.repository;

import com.b2g.lendservice.model.LendingOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LendingOptionRepository extends JpaRepository<LendingOption, UUID> {

}