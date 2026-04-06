package com.example.repository;

import com.example.model.EnhancementChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnhancementChecklistRepository extends JpaRepository<EnhancementChecklist, Long> {
    List<EnhancementChecklist> findByPropertyIdAndTypeOrderByCreatedAtAsc(Long propertyId, String type);
}
