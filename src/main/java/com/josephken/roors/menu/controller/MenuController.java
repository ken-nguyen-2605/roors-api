package com.josephken.roors.menu.controller;

import com.josephken.roors.auth.dto.MessageResponse;
import com.josephken.roors.menu.dto.MenuItemRequest;
import com.josephken.roors.menu.dto.MenuItemResponse;
import com.josephken.roors.menu.dto.DishRatingResponse;
import com.josephken.roors.menu.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<Page<MenuItemResponse>> getAllMenuItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("size: " + size);
        return ResponseEntity.ok(menuItemService.getAllMenuItems(page, size, sortBy, sortDir));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<MenuItemResponse>> getMenuItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByCategory(categoryId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<MenuItemResponse> getMenuItemBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(menuItemService.getMenuItemBySlug(slug));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MenuItemResponse>> searchMenuItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(menuItemService.searchMenuItems(keyword, page, size));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<Page<MenuItemResponse>> filterByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(menuItemService.filterByPriceRange(minPrice, maxPrice, page, size));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<MenuItemResponse>> getFeaturedMenuItems() {
        return ResponseEntity.ok(menuItemService.getFeaturedMenuItems());
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<MenuItemResponse>> getTopRatedMenuItems() {
        return ResponseEntity.ok(menuItemService.getTopRatedMenuItems());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MenuItemResponse>> getPopularMenuItems() {
        return ResponseEntity.ok(menuItemService.getPopularMenuItems());
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.createMenuItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, request));
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(new MessageResponse("Menu item deleted successfully"));
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<DishRatingResponse>> getDishRatings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(menuItemService.getDishRatings(id, limit));
    }
}
