package com.b2g.notificationservice.repository;

import com.b2g.notificationservice.model.UserNotificationBillboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BillboardRepository extends JpaRepository<UserNotificationBillboard, UUID> {
    Page<UserNotificationBillboard> findByUserid(UUID userid, Pageable pageable);
}
