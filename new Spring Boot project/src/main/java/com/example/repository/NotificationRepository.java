package com.example.repository;

import com.example.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Notification> findByOwnerIdAndIsReadOrderByCreatedAtDesc(Long ownerId, Boolean isRead);

    List<Notification> findByOwnerIdAndDueAtLessThanEqualOrderByCreatedAtDesc(Long ownerId, LocalDateTime dueAt);

    List<Notification> findByOwnerIdAndIsReadAndDueAtLessThanEqualOrderByCreatedAtDesc(
            Long ownerId,
            Boolean isRead,
            LocalDateTime dueAt
    );

    Optional<Notification> findByIdAndOwnerId(Long id, Long ownerId);

    long countByIsReadFalse();
}
