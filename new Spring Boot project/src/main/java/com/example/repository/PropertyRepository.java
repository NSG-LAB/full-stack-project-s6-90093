package com.example.repository;

import com.example.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	List<Property> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
