package com.josephken.roors.config;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.entity.UserRole;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.menu.entity.Category;
import com.josephken.roors.menu.entity.MenuItem;
import com.josephken.roors.menu.repository.CategoryRepository;
import com.josephken.roors.menu.repository.MenuItemRepository;
import com.josephken.roors.reservation.entity.DiningTable;
import com.josephken.roors.reservation.entity.DiningTableStatus;
import com.josephken.roors.reservation.repository.DiningTableRepository;
import com.josephken.roors.order.entity.Order;
import com.josephken.roors.order.entity.OrderItem;
import com.josephken.roors.order.entity.OrderStatus;
import com.josephken.roors.order.entity.OrderType;
import com.josephken.roors.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final DiningTableRepository diningTableRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        if (shouldInitializeData()) {
            initializeUsers();
            initializeCategories();
            initializeMenuItems();
            initializeDiningTables();
            initializeOrders();
            log.info("Sample data initialization completed successfully!");
        } else {
            log.info("Data already exists. Updating phone numbers in existing orders...");
            updateExistingOrderPhoneNumbers();
        }
    }

    private boolean shouldInitializeData() {
        // Always initialize if no orders exist, even if users/categories exist
        return orderRepository.count() == 0;
    }

    private void initializeUsers() {
        log.info("Initializing sample users...");
        
        // Skip if users already exist
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user initialization");
            return;
        }
        
        // Admin/Manager user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@roors.com");
        admin.setFullName("Admin User");
        admin.setContactNumber("0123456789");
        admin.setAddress("123 Admin Street, City Center");
        admin.setRole(UserRole.MANAGER);
        admin.setVerified(true);
        admin.setMemberSince(LocalDateTime.now().minusMonths(12));
        
        // Staff user
        User staff = new User();
        staff.setUsername("staff");
        staff.setPassword(passwordEncoder.encode("staff123"));
        staff.setEmail("staff@roors.com");
        staff.setFullName("Staff Member");
        staff.setContactNumber("0987654321");
        staff.setAddress("456 Staff Avenue, Downtown");
        staff.setRole(UserRole.STAFF);
        staff.setVerified(true);
        staff.setMemberSince(LocalDateTime.now().minusMonths(6));
        
        // Sample customers
        User customer1 = new User();
        customer1.setUsername("john_doe");
        customer1.setPassword(passwordEncoder.encode("password123"));
        customer1.setEmail("john.doe@example.com");
        customer1.setFullName("John Doe");
        customer1.setContactNumber("0111222333");
        customer1.setAddress("789 Customer Lane, Suburb");
        customer1.setRole(UserRole.CUSTOMER);
        customer1.setVerified(true);
        customer1.setMemberSince(LocalDateTime.now().minusMonths(3));
        
        User customer2 = new User();
        customer2.setUsername("jane_smith");
        customer2.setPassword(passwordEncoder.encode("password123"));
        customer2.setEmail("jane.smith@example.com");
        customer2.setFullName("Jane Smith");
        customer2.setContactNumber("0222333444");
        customer2.setAddress("321 Food Lover Street, City");
        customer2.setRole(UserRole.CUSTOMER);
        customer2.setVerified(true);
        customer2.setMemberSince(LocalDateTime.now().minusWeeks(2));
        
        userRepository.saveAll(Arrays.asList(admin, staff, customer1, customer2));
        log.info("Created {} sample users", 4);
    }

    private void initializeCategories() {
        log.info("Initializing menu categories...");
        
        // Skip if categories already exist
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping category initialization");
            return;
        }
        
        List<Category> categories = Arrays.asList(
            createCategory("Appetizers", "appetizers", "Start your meal with our delicious appetizers", 1),
            createCategory("Main Courses", "main-courses", "Hearty and satisfying main dishes", 2),
            createCategory("Desserts", "desserts", "Sweet treats to end your meal perfectly", 3),
            createCategory("Beverages", "beverages", "Refreshing drinks and beverages", 4),
            createCategory("Salads", "salads", "Fresh and healthy salad options", 5),
            createCategory("Soups", "soups", "Warm and comforting soups", 6)
        );
        
        categoryRepository.saveAll(categories);
        log.info("Created {} categories", categories.size());
    }

    private Category createCategory(String name, String slug, String description, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setDisplayOrder(displayOrder);
        category.setIsActive(true);
        return category;
    }

    private void initializeMenuItems() {
        log.info("Initializing menu items...");
        
        // Skip if menu items already exist
        if (menuItemRepository.count() > 0) {
            log.info("Menu items already exist, skipping menu item initialization");
            return;
        }
        
        Category appetizers = categoryRepository.findBySlug("appetizers").orElse(null);
        Category mainCourses = categoryRepository.findBySlug("main-courses").orElse(null);
        Category desserts = categoryRepository.findBySlug("desserts").orElse(null);
        Category beverages = categoryRepository.findBySlug("beverages").orElse(null);
        Category salads = categoryRepository.findBySlug("salads").orElse(null);
        Category soups = categoryRepository.findBySlug("soups").orElse(null);
        
        List<MenuItem> menuItems = Arrays.asList(
            // Appetizers
            createMenuItem("Spring Rolls", "spring-rolls", "Crispy vegetable spring rolls served with sweet chili sauce", 
                new BigDecimal("8.99"), appetizers, 15, 1, "Cabbage, carrots, bean sprouts, wrapper", "Contains gluten", 150),
            createMenuItem("Chicken Wings", "chicken-wings", "Spicy buffalo chicken wings with ranch dip", 
                new BigDecimal("12.99"), appetizers, 20, 3, "Chicken wings, buffalo sauce, celery", "None", 280),
            createMenuItem("Mozzarella Sticks", "mozzarella-sticks", "Golden fried mozzarella with marinara sauce", 
                new BigDecimal("9.99"), appetizers, 12, 0, "Mozzarella cheese, breadcrumbs", "Contains dairy, gluten", 320),
                
            // Main Courses
            createMenuItem("Grilled Salmon", "grilled-salmon", "Fresh Atlantic salmon with herbs and lemon", 
                new BigDecimal("24.99"), mainCourses, 25, 0, "Salmon fillet, herbs, lemon, vegetables", "Fish", 450),
            createMenuItem("Beef Steak", "beef-steak", "Premium ribeye steak cooked to perfection", 
                new BigDecimal("32.99"), mainCourses, 30, 1, "Ribeye steak, garlic, rosemary", "None", 680),
            createMenuItem("Chicken Pasta", "chicken-pasta", "Creamy alfredo pasta with grilled chicken", 
                new BigDecimal("18.99"), mainCourses, 20, 0, "Pasta, chicken breast, alfredo sauce", "Contains dairy, gluten", 520),
            createMenuItem("Vegetarian Pizza", "vegetarian-pizza", "Wood-fired pizza with fresh vegetables", 
                new BigDecimal("16.99"), mainCourses, 18, 0, "Pizza dough, tomatoes, bell peppers, mushrooms", "Contains gluten, dairy", 380),
                
            // Salads
            createMenuItem("Caesar Salad", "caesar-salad", "Classic Caesar salad with parmesan and croutons", 
                new BigDecimal("11.99"), salads, 10, 0, "Romaine lettuce, parmesan, croutons, Caesar dressing", "Contains dairy, gluten", 220),
            createMenuItem("Greek Salad", "greek-salad", "Fresh Mediterranean salad with feta cheese", 
                new BigDecimal("13.99"), salads, 8, 0, "Mixed greens, tomatoes, olives, feta cheese", "Contains dairy", 180),
                
            // Soups
            createMenuItem("Tomato Soup", "tomato-soup", "Creamy tomato soup with fresh basil", 
                new BigDecimal("7.99"), soups, 15, 0, "Tomatoes, cream, basil, onions", "Contains dairy", 160),
            createMenuItem("Chicken Noodle Soup", "chicken-noodle-soup", "Hearty soup with chicken and vegetables", 
                new BigDecimal("9.99"), soups, 20, 0, "Chicken broth, noodles, carrots, celery", "Contains gluten", 190),
                
            // Desserts
            createMenuItem("Chocolate Cake", "chocolate-cake", "Rich chocolate cake with berry compote", 
                new BigDecimal("8.99"), desserts, 5, 0, "Chocolate, flour, eggs, berries", "Contains dairy, eggs, gluten", 420),
            createMenuItem("Tiramisu", "tiramisu", "Classic Italian tiramisu with coffee and mascarpone", 
                new BigDecimal("9.99"), desserts, 5, 0, "Mascarpone, coffee, ladyfingers", "Contains dairy, eggs", 380),
            createMenuItem("Ice Cream Sundae", "ice-cream-sundae", "Vanilla ice cream with chocolate sauce and nuts", 
                new BigDecimal("6.99"), desserts, 3, 0, "Vanilla ice cream, chocolate sauce, nuts", "Contains dairy, nuts", 290),
                
            // Beverages
            createMenuItem("Fresh Orange Juice", "fresh-orange-juice", "Freshly squeezed orange juice", 
                new BigDecimal("4.99"), beverages, 5, 0, "Fresh oranges", "None", 110),
            createMenuItem("Coffee", "coffee", "Freshly brewed Colombian coffee", 
                new BigDecimal("3.99"), beverages, 5, 0, "Coffee beans", "None", 5),
            createMenuItem("Green Tea", "green-tea", "Organic green tea with antioxidants", 
                new BigDecimal("3.49"), beverages, 3, 0, "Green tea leaves", "None", 2),
            createMenuItem("Soft Drinks", "soft-drinks", "Choice of Coca-Cola, Sprite, or Fanta", 
                new BigDecimal("2.99"), beverages, 1, 0, "Carbonated water, flavoring", "None", 140)
        );
        
        menuItemRepository.saveAll(menuItems);
        log.info("Created {} menu items", menuItems.size());
    }

    private MenuItem createMenuItem(String name, String slug, String description, BigDecimal price, 
                                  Category category, int prepTime, int spicyLevel, String ingredients, 
                                  String allergens, int calories) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setSlug(slug);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setPreparationTime(prepTime);
        item.setSpicyLevel(spicyLevel);
        item.setIngredients(ingredients);
        item.setAllergens(allergens);
        item.setCalories(calories);
        item.setIsAvailable(true);
        item.setIsFeatured(Math.random() > 0.7); // 30% chance to be featured
        item.setRating(3.5 + Math.random() * 1.5); // Random rating between 3.5-5.0
        item.setReviewCount((int) (Math.random() * 100)); // Random review count
        item.setOrderCount((int) (Math.random() * 500)); // Random order count
        item.setServingSize("1 portion");
        return item;
    }

    private void initializeDiningTables() {
        log.info("Initializing dining tables...");
        
        // Skip if tables already exist
        if (diningTableRepository.count() > 0) {
            log.info("Dining tables already exist, skipping table initialization");
            return;
        }
        
        List<DiningTable> tables = Arrays.asList(
            // Ground Floor Tables
            createTable("T01", "Ground Floor", 2, DiningTableStatus.OPEN),
            createTable("T02", "Ground Floor", 2, DiningTableStatus.OPEN),
            createTable("T03", "Ground Floor", 4, DiningTableStatus.OPEN),
            createTable("T04", "Ground Floor", 4, DiningTableStatus.OPEN),
            createTable("T05", "Ground Floor", 4, DiningTableStatus.OPEN),
            createTable("T06", "Ground Floor", 6, DiningTableStatus.OPEN),
            createTable("T07", "Ground Floor", 6, DiningTableStatus.OPEN),
            createTable("T08", "Ground Floor", 8, DiningTableStatus.OPEN),
            
            // First Floor Tables
            createTable("T09", "First Floor", 2, DiningTableStatus.OPEN),
            createTable("T10", "First Floor", 2, DiningTableStatus.OPEN),
            createTable("T11", "First Floor", 4, DiningTableStatus.OPEN),
            createTable("T12", "First Floor", 4, DiningTableStatus.OPEN),
            createTable("T13", "First Floor", 6, DiningTableStatus.OPEN),
            createTable("T14", "First Floor", 6, DiningTableStatus.OPEN),
            createTable("T15", "First Floor", 8, DiningTableStatus.OPEN),
            createTable("T16", "First Floor", 10, DiningTableStatus.OPEN),
            
            // VIP Section
            createTable("VIP01", "VIP Section", 4, DiningTableStatus.OPEN),
            createTable("VIP02", "VIP Section", 6, DiningTableStatus.OPEN),
            createTable("VIP03", "VIP Section", 8, DiningTableStatus.OPEN),
            
            // Outdoor Terrace
            createTable("OUT01", "Outdoor Terrace", 4, DiningTableStatus.OPEN),
            createTable("OUT02", "Outdoor Terrace", 4, DiningTableStatus.OPEN),
            createTable("OUT03", "Outdoor Terrace", 6, DiningTableStatus.CLOSED) // Under maintenance
        );
        
        diningTableRepository.saveAll(tables);
        log.info("Created {} dining tables", tables.size());
    }

    private DiningTable createTable(String name, String floor, int capacity, DiningTableStatus status) {
        return DiningTable.builder()
                .name(name)
                .floor(floor)
                .capacity(capacity)
                .status(status)
                .build();
    }

    private void initializeOrders() {
        log.info("Initializing sample orders...");
        
        // Get users and menu items for creating orders
        List<User> customers = userRepository.findByRole(UserRole.CUSTOMER);
        List<MenuItem> menuItems = menuItemRepository.findAll();
        
        if (customers.isEmpty()) {
            log.warn("No customers found. Creating sample customers first.");
            // Create at least one customer for orders
            User sampleCustomer = new User();
            sampleCustomer.setUsername("sample_customer");
            sampleCustomer.setPassword(passwordEncoder.encode("password123"));
            sampleCustomer.setEmail("sample@example.com");
            sampleCustomer.setFullName("Sample Customer");
            sampleCustomer.setContactNumber("0999888777");
            sampleCustomer.setAddress("123 Sample Address");
            sampleCustomer.setRole(UserRole.CUSTOMER);
            sampleCustomer.setVerified(true);
            sampleCustomer.setMemberSince(LocalDateTime.now().minusMonths(1));
            userRepository.save(sampleCustomer);
            customers = Arrays.asList(sampleCustomer);
        }
        
        if (menuItems.isEmpty()) {
            log.warn("No menu items found. Cannot create orders without menu items.");
            return;
        }

        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();
        
        // Create 15 sample orders with different statuses and scenarios
        // Use modulo to cycle through available customers safely
        int numCustomers = customers.size();
        int numMenuItems = menuItems.size();
        
        List<Order> orders = Arrays.asList(
            // Recent completed orders with ratings
            createSampleOrderWithRating("ORD-001", customers.get(0 % numCustomers), OrderStatus.COMPLETED, now.minusDays(2), 
                getSafeMenuItemSubList(menuItems, 0, 3), "Please make it spicy", "Alice Johnson", 5, "Absolutely delicious! The spice level was perfect."),
                
            createSampleOrderWithRating("ORD-002", customers.get(1 % numCustomers), OrderStatus.COMPLETED, now.minusDays(1), 
                getSafeMenuItemSubList(menuItems, 2, 5), "No onions please", "Bob Martinez", 4, "Great food, exactly as requested. Quick service!"),
                
            createSampleOrderWithRating("ORD-003", customers.get(0 % numCustomers), OrderStatus.COMPLETED, now.minusHours(8), 
                getSafeMenuItemSubList(menuItems, 1, 4), "Extra sauce on the side", "Catherine Wong", 4, "Good portions and the extra sauce was appreciated."),

            // Current active orders
            createSampleOrder("ORD-004", customers.get(2 % numCustomers), OrderStatus.PREPARING, now.minusMinutes(15), 
                getSafeMenuItemSubList(menuItems, 3, 6), "Medium spice level", "David Thompson"),
                
            createSampleOrder("ORD-005", customers.get(1 % numCustomers), OrderStatus.READY, now.minusMinutes(5), 
                getSafeMenuItemSubList(menuItems, 0, 2), "Quick delivery please", "Emma Rodriguez"),
                
            createSampleOrder("ORD-006", customers.get(3 % numCustomers), OrderStatus.READY, now.minusMinutes(20), 
                getSafeMenuItemSubList(menuItems, 4, 7), "Call when arrived", "Frank Williams"),

            // Pending orders
            createSampleOrder("ORD-007", customers.get(2 % numCustomers), OrderStatus.PENDING, now.minusMinutes(2), 
                getSafeMenuItemSubList(menuItems, 1, 3), "No mushrooms", "Grace Lee"),
                
            createSampleOrder("ORD-008", customers.get(0 % numCustomers), OrderStatus.PENDING, now.minusMinutes(1), 
                getSafeMenuItemSubList(menuItems, 5, 8), "Extra vegetables", "Henry Davis"),

            // Cancelled orders (for variety)
            createSampleOrder("ORD-009", customers.get(1 % numCustomers), OrderStatus.CANCELLED, now.minusHours(2), 
                getSafeMenuItemSubList(menuItems, 2, 4), "Wrong address - cancelled", "Isabella Garcia"),
                
            createSampleOrder("ORD-010", customers.get(3 % numCustomers), OrderStatus.CANCELLED, now.minusDays(1), 
                getSafeMenuItemSubList(menuItems, 0, 3), "Customer requested cancellation", "Jack Wilson"),

            // Large orders with ratings
            createSampleOrderWithRating("ORD-011", customers.get(2 % numCustomers), OrderStatus.COMPLETED, now.minusDays(3), 
                getSafeMenuItemSubList(menuItems, 0, Math.min(8, numMenuItems)), "Family dinner - large portions", "Karen Brown", 5, "Perfect for our family gathering! Everything was hot and fresh."),
                
            createSampleOrder("ORD-012", customers.get(0 % numCustomers), OrderStatus.PREPARING, now.minusMinutes(30), 
                getSafeMenuItemSubList(menuItems, 2, Math.min(9, numMenuItems)), "Office lunch order", "Liam Anderson"),

            // Today's orders
            createSampleOrder("ORD-013", customers.get(1 % numCustomers), OrderStatus.READY, now.minusMinutes(10), 
                getSafeMenuItemSubList(menuItems, 1, 4), "Table service preferred", "Maria Gonzalez"),
                
            createSampleOrder("ORD-014", customers.get(3 % numCustomers), OrderStatus.PREPARING, now.minusMinutes(25), 
                getSafeMenuItemSubList(menuItems, 3, 6), "Handle with care - fragile items", "Noah Johnson"),
                
            createSampleOrderWithRating("ORD-015", customers.get(2 % numCustomers), OrderStatus.COMPLETED, now.minusHours(1), 
                getSafeMenuItemSubList(menuItems, 4, 6), "Thank you for excellent service!", "Olivia Taylor", 5, "Outstanding service and delicious food. Will definitely order again!")
        );

        orderRepository.saveAll(orders);
        log.info("Created {} sample orders with various statuses", orders.size());
    }

    private Order createSampleOrder(String orderNumber, User customer, OrderStatus status, 
                                   LocalDateTime orderTime, List<MenuItem> selectedItems, String instructions, String customerName) {
        Order order = createSampleOrderBase(orderNumber, customer, status, orderTime, selectedItems, instructions, customerName);
        return order;
    }
    
    private Order createSampleOrderWithRating(String orderNumber, User customer, OrderStatus status, 
                                   LocalDateTime orderTime, List<MenuItem> selectedItems, String instructions, 
                                   String customerName, int rating, String feedback) {
        Order order = createSampleOrderBase(orderNumber, customer, status, orderTime, selectedItems, instructions, customerName);
        order.setRating(rating);
        order.setFeedback(feedback);
        order.setRatedAt(orderTime.plusHours(2)); // Assume rating was given 2 hours after order
        return order;
    }

    private Order createSampleOrderBase(String orderNumber, User customer, OrderStatus status, 
                                   LocalDateTime orderTime, List<MenuItem> selectedItems, String instructions, String customerName) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(customer);
        order.setStatus(status);
        // Randomly assign order type for variety
        OrderType[] types = OrderType.values();
        order.setOrderType(types[Math.abs(customerName.hashCode()) % types.length]);
        order.setCreatedAt(orderTime);
        order.setUpdatedAt(orderTime.plusMinutes(5));
        order.setCustomerName(customerName);
        
        // Create order items
        BigDecimal subtotal = BigDecimal.ZERO;
        Random random = new Random();
        
        for (MenuItem menuItem : selectedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setUnitPrice(menuItem.getPrice());
            
            int quantity = random.nextInt(3) + 1; // 1-3 items
            orderItem.setQuantity(quantity);
            
            BigDecimal itemSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setSpecialInstructions(instructions);
            orderItem.setCreatedAt(orderTime);
            
            order.getOrderItems().add(orderItem);
            subtotal = subtotal.add(itemSubtotal);
        }
        
        // Calculate totals
        BigDecimal taxRate = BigDecimal.valueOf(0.08); // 8% tax
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal deliveryFee = BigDecimal.valueOf(2.50);
        BigDecimal totalAmount = subtotal.add(taxAmount).add(deliveryFee);
        
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(totalAmount);
        order.setSpecialInstructions(instructions);
        
        // Set delivery details
        order.setDeliveryAddress("123 Sample Street, City, State 12345");
        order.setCustomerPhone("555-" + (1000 + Math.abs(customerName.hashCode() % 9000))); // Generate phone based on name
        
        return order;
    }
    
    private List<MenuItem> getSafeMenuItemSubList(List<MenuItem> menuItems, int start, int end) {
        int size = menuItems.size();
        if (size == 0) return menuItems;
        
        int safeStart = Math.min(start, size - 1);
        int safeEnd = Math.min(end, size);
        
        // Ensure we have at least one item
        if (safeStart >= safeEnd) {
            safeEnd = Math.min(safeStart + 1, size);
        }
        
        return menuItems.subList(safeStart, safeEnd);
    }
    
    private void updateExistingOrderPhoneNumbers() {
        log.info("Updating phone numbers in existing orders...");
        
        // Get all orders
        var orders = orderRepository.findAll();
        String[] customerNames = {
            "John Smith", "Emily Johnson", "Michael Brown", "Sarah Davis", 
            "David Wilson", "Lisa Anderson", "James Taylor", "Jessica Martinez"
        };
        
        for (int i = 0; i < orders.size(); i++) {
            var order = orders.get(i);
            String customerName = customerNames[i % customerNames.length];
            
            // Update phone number based on customer name
            String phoneNumber = "555-" + (1000 + Math.abs(customerName.hashCode() % 9000));
            order.setCustomerPhone(phoneNumber);
            order.setCustomerName(customerName);
            
            // Add some ratings to completed orders
            if (order.getStatus().toString().equals("COMPLETED")) {
                order.setRating(3 + (int)(Math.random() * 3)); // Rating 3-5
                order.setFeedback(generateFeedback(order.getRating()));
                order.setRatedAt(order.getCreatedAt().plusHours(1));
            }
        }
        
        orderRepository.saveAll(orders);
        log.info("Updated {} orders with realistic customer names and phone numbers", orders.size());
    }
    
    private String generateFeedback(Integer rating) {
        String[] excellentFeedback = {
            "Outstanding food and service! Will definitely order again.",
            "Perfect meal, everything was delicious and arrived hot.",
            "Excellent quality and fast delivery. Highly recommended!"
        };
        
        String[] goodFeedback = {
            "Good food, satisfied with the order.",
            "Nice flavors, delivery was on time.",
            "Enjoyed the meal, good portion sizes."
        };
        
        String[] averageFeedback = {
            "Food was okay, could be better.",
            "Average taste, nothing special.",
            "Decent meal but room for improvement."
        };
        
        if (rating >= 5) {
            return excellentFeedback[(int)(Math.random() * excellentFeedback.length)];
        } else if (rating >= 4) {
            return goodFeedback[(int)(Math.random() * goodFeedback.length)];
        } else {
            return averageFeedback[(int)(Math.random() * averageFeedback.length)];
        }
    }
}