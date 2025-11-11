package com.josephken.roors.menu.repository;

import com.josephken.roors.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    boolean existsBySlug(String slug);
}
