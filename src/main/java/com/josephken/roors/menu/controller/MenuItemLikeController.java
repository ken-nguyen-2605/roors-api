package com.josephken.roors.menu.controller;

import com.josephken.roors.auth.dto.MessageResponse;
import com.josephken.roors.menu.dto.MenuItemResponse;
import com.josephken.roors.menu.service.MenuItemLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/menu/likes")
@RequiredArgsConstructor
public class MenuItemLikeController {

    private final MenuItemLikeService menuItemLikeService;

    @PostMapping("/{menuItemId}")
    public ResponseEntity<MessageResponse> likeMenuItem(@PathVariable Long menuItemId) {
        menuItemLikeService.likeMenuItem(menuItemId);
        return ResponseEntity.ok(new MessageResponse("Menu item liked successfully"));
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<MessageResponse> unlikeMenuItem(@PathVariable Long menuItemId) {
        menuItemLikeService.unlikeMenuItem(menuItemId);
        return ResponseEntity.ok(new MessageResponse("Menu item unliked successfully"));
    }

    @GetMapping("/{menuItemId}/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Long menuItemId) {
        boolean isLiked = menuItemLikeService.isLikedByCurrentUser(menuItemId);
        long likeCount = menuItemLikeService.getLikeCount(menuItemId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getLikedMenuItems() {
        return ResponseEntity.ok(menuItemLikeService.getLikedMenuItems());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<MenuItemResponse>> getLikedMenuItemsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuItemLikeService.getLikedMenuItems(page, size));
    }

    @GetMapping("/{menuItemId}/count")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable Long menuItemId) {
        long count = menuItemLikeService.getLikeCount(menuItemId);
        Map<String, Long> response = new HashMap<>();
        response.put("likeCount", count);
        return ResponseEntity.ok(response);
    }
}

