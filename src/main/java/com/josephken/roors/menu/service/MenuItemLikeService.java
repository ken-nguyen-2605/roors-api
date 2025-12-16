package com.josephken.roors.menu.service;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.auth.util.AuthenticationHelper;
import com.josephken.roors.menu.dto.MenuItemResponse;
import com.josephken.roors.menu.entity.MenuItem;
import com.josephken.roors.menu.entity.UserMenuItemLike;
import com.josephken.roors.menu.repository.MenuItemRepository;
import com.josephken.roors.menu.repository.UserMenuItemLikeRepository;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuItemLikeService {

    private final UserMenuItemLikeRepository userMenuItemLikeRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final MenuItemService menuItemService;

    @Transactional
    public void likeMenuItem(Long menuItemId) {
        User user = AuthenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User must be authenticated to like menu items"));
        
        // Fetch the user from repository to ensure it's managed
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

        // Check if already liked
        if (userMenuItemLikeRepository.existsByUserAndMenuItem(managedUser, menuItem)) {
            log.info(LogCategory.menu("User " + managedUser.getId() + " already liked menu item " + menuItemId));
            return;
        }

        UserMenuItemLike like = new UserMenuItemLike();
        like.setUser(managedUser);
        like.setMenuItem(menuItem);
        userMenuItemLikeRepository.save(like);
        
        log.info(LogCategory.menu("User " + managedUser.getId() + " liked menu item " + menuItemId));
    }

    @Transactional
    public void unlikeMenuItem(Long menuItemId) {
        User user = AuthenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User must be authenticated to unlike menu items"));
        
        // Fetch the user from repository to ensure it's managed
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

        UserMenuItemLike like = userMenuItemLikeRepository.findByUserAndMenuItem(managedUser, menuItem)
                .orElseThrow(() -> new RuntimeException("Menu item is not liked by the user"));

        userMenuItemLikeRepository.delete(like);
        
        log.info(LogCategory.menu("User " + managedUser.getId() + " unliked menu item " + menuItemId));
    }

    @Transactional(readOnly = true)
    public boolean isLikedByCurrentUser(Long menuItemId) {
        return AuthenticationHelper.getCurrentUser()
                .map(user -> {
                    User managedUser = userRepository.findById(user.getId()).orElse(null);
                    if (managedUser == null) return false;
                    
                    MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);
                    if (menuItem == null) return false;
                    
                    return userMenuItemLikeRepository.existsByUserAndMenuItem(managedUser, menuItem);
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getLikedMenuItems() {
        User user = AuthenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User must be authenticated to view liked menu items"));
        
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MenuItem> likedMenuItems = userMenuItemLikeRepository.findLikedMenuItemsByUser(managedUser);
        
        return likedMenuItems.stream()
                .map(item -> enrichWithLikeInfo(menuItemService.mapToResponse(item), item.getId(), true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getLikedMenuItems(int page, int size) {
        User user = AuthenticationHelper.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User must be authenticated to view liked menu items"));
        
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuItem> likedMenuItems = userMenuItemLikeRepository.findLikedMenuItemsByUser(managedUser, pageable);
        
        return likedMenuItems.map(item -> enrichWithLikeInfo(menuItemService.mapToResponse(item), item.getId(), true));
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));
        
        return userMenuItemLikeRepository.countByMenuItem(menuItem);
    }

    /**
     * Enriches MenuItemResponse with like information (isLiked and likeCount)
     * @param response The MenuItemResponse to enrich
     * @param menuItemId The menu item ID
     * @param isLikedKnown If true, sets isLiked to true directly (optimization for already-liked items)
     */
    private MenuItemResponse enrichWithLikeInfo(MenuItemResponse response, Long menuItemId, boolean isLikedKnown) {
        response.setIsLiked(isLikedKnown || isLikedByCurrentUser(menuItemId));
        response.setLikeCount(getLikeCount(menuItemId));
        return response;
    }
}

