package com.booking.resourcebooking.repository;

import com.booking.resourcebooking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByAvailable(Boolean available);
    Optional<Resource> findByName(String name);
}
