package com.josephken.roors.menu.service;

import com.josephken.roors.menu.dto.MenuItemRequest;
import com.josephken.roors.menu.dto.MenuItemResponse;
import com.josephken.roors.menu.dto.CategoryResponse;
import com.josephken.roors.menu.entity.Category;
import com.josephken.roors.menu.entity.MenuItem;
import com.josephken.roors.menu.repository.CategoryRepository;
import com.josephken.roors.menu.repository.MenuItemRepository;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getAllMenuItems(int page, int size, String sortBy, String sortDir) {
        log.info(LogCategory.menu("Fetching all menu items"));
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return menuItemRepository.findByIsAvailableTrue(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByCategory(Long categoryId, int page, int size) {
        log.info(LogCategory.menu("Fetching menu items for category ID: " + categoryId));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        
        Pageable pageable = PageRequest.of(page, size);
        return menuItemRepository.findByCategoryAndIsAvailableTrue(category, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        log.info(LogCategory.menu("Fetching menu item with ID: " + id));
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
        return mapToResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemBySlug(String slug) {
        log.info(LogCategory.menu("Fetching menu item with slug: " + slug));
        MenuItem menuItem = menuItemRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Menu item not found with slug: " + slug));
        return mapToResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> searchMenuItems(String keyword, int page, int size) {
        log.info(LogCategory.menu("Searching menu items with keyword: " + keyword));
        Pageable pageable = PageRequest.of(page, size);
        return menuItemRepository.searchAvailableMenuItems(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        log.info(LogCategory.menu(
                String.format("Filtering menu items by price range: %s - %s", minPrice, maxPrice)));
        
        Pageable pageable = PageRequest.of(page, size);
        return menuItemRepository.findByPriceRangeAndIsAvailableTrue(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getFeaturedMenuItems() {
        log.info(LogCategory.menu("Fetching featured menu items"));
        return menuItemRepository.findByIsFeaturedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getTopRatedMenuItems() {
        log.info(LogCategory.menu("Fetching top rated menu items"));
        return menuItemRepository.findTop10ByIsAvailableTrueOrderByRatingDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getPopularMenuItems() {
        log.info(LogCategory.menu("Fetching popular menu items"));
        return menuItemRepository.findTop10ByIsAvailableTrueOrderByOrderCountDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info(LogCategory.menu("Creating new menu item: " + request.getName()));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        String slug = generateSlug(request.getName());
        if (menuItemRepository.existsBySlug(slug)) {
            throw new RuntimeException("Menu item with this name already exists");
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setSlug(slug);
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(category);
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        menuItem.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setSpicyLevel(request.getSpicyLevel() != null ? request.getSpicyLevel() : 0);
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setCalories(request.getCalories());
        menuItem.setServingSize(request.getServingSize());

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        log.info(LogCategory.menu("Menu item created successfully with ID: " + savedMenuItem.getId()));
        
        return mapToResponse(savedMenuItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        log.info(LogCategory.menu("Updating menu item with ID: " + id));
        
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        // Check slug uniqueness if name changed
        if (!menuItem.getName().equals(request.getName())) {
            String newSlug = generateSlug(request.getName());
            if (!newSlug.equals(menuItem.getSlug()) && menuItemRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Menu item with this name already exists");
            }
            menuItem.setName(request.getName());
            menuItem.setSlug(newSlug);
        }

        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(category);
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setIsAvailable(request.getIsAvailable());
        menuItem.setIsFeatured(request.getIsFeatured());
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setSpicyLevel(request.getSpicyLevel());
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setCalories(request.getCalories());
        menuItem.setServingSize(request.getServingSize());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info(LogCategory.menu("Menu item updated successfully with ID: " + id));
        
        return mapToResponse(updatedMenuItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        log.info(LogCategory.menu("Deleting menu item with ID: " + id));
        
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Menu item not found with ID: " + id);
        }

        menuItemRepository.deleteById(id);
        log.info(LogCategory.menu("Menu item deleted successfully with ID: " + id));
    }

    @Transactional
    public MenuItemResponse toggleAvailability(Long id) {
        log.info(LogCategory.menu("Toggling availability for menu item with ID: " + id));
        
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));

        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        
        log.info(LogCategory.menu(
                String.format("Menu item ID %d availability changed to: %s", id, updatedMenuItem.getIsAvailable())));
        
        return mapToResponse(updatedMenuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        CategoryResponse categoryResponse = new CategoryResponse(
                menuItem.getCategory().getId(),
                menuItem.getCategory().getName(),
                menuItem.getCategory().getSlug(),
                menuItem.getCategory().getDescription(),
                menuItem.getCategory().getImageUrl(),
                menuItem.getCategory().getDisplayOrder(),
                menuItem.getCategory().getIsActive(),
                menuItem.getCategory().getCreatedAt(),
                menuItem.getCategory().getUpdatedAt()
        );

        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getSlug(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                categoryResponse,
                menuItem.getImageUrl(),
                menuItem.getIsAvailable(),
                menuItem.getIsFeatured(),
                menuItem.getPreparationTime(),
                menuItem.getSpicyLevel(),
                menuItem.getIngredients(),
                menuItem.getAllergens(),
                menuItem.getCalories(),
                menuItem.getServingSize(),
                menuItem.getRating(),
                menuItem.getReviewCount(),
                menuItem.getOrderCount(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
