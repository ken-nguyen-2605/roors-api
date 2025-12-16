package com.josephken.roors.menu.repository;

import com.josephken.roors.menu.entity.Category;
import com.josephken.roors.menu.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    Optional<MenuItem> findBySlug(String slug);
    
    List<MenuItem> findByCategory(Category category);
    
    List<MenuItem> findByCategoryAndIsAvailableTrue(Category category);
    
    List<MenuItem> findByIsAvailableTrue();
    
    List<MenuItem> findByIsFeaturedTrue();
    
    Page<MenuItem> findByIsAvailableTrue(Pageable pageable);
    
    Page<MenuItem> findByCategoryAndIsAvailableTrue(Category category, Pageable pageable);
    
    @Query("SELECT m FROM MenuItem m WHERE " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "m.isAvailable = true")
    Page<MenuItem> searchAvailableMenuItems(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MenuItem> searchAllMenuItems(@Param("keyword") String keyword, Pageable pageable);

    Page<MenuItem> findByCategory(Category category, Pageable pageable);
    
    @Query("SELECT m FROM MenuItem m WHERE " +
           "m.price BETWEEN :minPrice AND :maxPrice AND " +
           "m.isAvailable = true")
    Page<MenuItem> findByPriceRangeAndIsAvailableTrue(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    List<MenuItem> findTop10ByIsAvailableTrueOrderByRatingDesc();
    
    List<MenuItem> findTop10ByIsAvailableTrueOrderByOrderCountDesc();
    
    boolean existsBySlug(String slug);
}
