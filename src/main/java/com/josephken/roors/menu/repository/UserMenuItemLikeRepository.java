package com.josephken.roors.menu.repository;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.menu.entity.MenuItem;
import com.josephken.roors.menu.entity.UserMenuItemLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMenuItemLikeRepository extends JpaRepository<UserMenuItemLike, Long> {
    
    Optional<UserMenuItemLike> findByUserAndMenuItem(User user, MenuItem menuItem);
    
    boolean existsByUserAndMenuItem(User user, MenuItem menuItem);
    
    List<UserMenuItemLike> findByUser(User user);
    
    Page<UserMenuItemLike> findByUser(User user, Pageable pageable);
    
    @Query("SELECT l.menuItem FROM UserMenuItemLike l WHERE l.user = :user")
    List<MenuItem> findLikedMenuItemsByUser(@Param("user") User user);
    
    @Query("SELECT l.menuItem FROM UserMenuItemLike l WHERE l.user = :user")
    Page<MenuItem> findLikedMenuItemsByUser(@Param("user") User user, Pageable pageable);
    
    long countByMenuItem(MenuItem menuItem);
}

