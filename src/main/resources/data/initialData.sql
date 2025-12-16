-- =========================================================
-- 0. CLEANUP
-- =========================================================
TRUNCATE TABLE
    users,
    categories,
    menu_items,
    dining_table,
    reservation,
    orders,
    order_items,
    payments,
    sepay_transactions,
    user_liked_dishes,
    user_menu_item_likes
    RESTART IDENTITY CASCADE;

-- =========================================================
-- 2. CATEGORIES
-- =========================================================
INSERT INTO categories (name, slug, description, image_url, display_order, is_active, created_at, updated_at) VALUES
                                                                                                                  ('Appetizers', 'appetizers', 'Start your meal with our delicious appetizers', '/dishes/dish1.jpg', 1, true, NOW(), NOW()),
                                                                                                                  ('Main Course', 'main-course', 'Hearty and satisfying main dishes', '/dishes/dish3.jpg', 2, true, NOW(), NOW()),
                                                                                                                  ('Beverages', 'beverages', 'Refreshing drinks and coffee', '/dishes/dish5.jpg', 3, true, NOW(), NOW()),
                                                                                                                  ('Desserts', 'desserts', 'Sweet treats to finish your meal', '/dishes/dish7.jpg', 4, true, NOW(), NOW()),
                                                                                                                  ('Vegetarian', 'vegetarian', 'Fresh and healthy meat-free options', '/dishes/dish9.jpg', 5, true, NOW(), NOW());

-- =========================================================
-- 3. MENU ITEMS
-- =========================================================
INSERT INTO menu_items (
    name, slug, description, price, image_url,
    is_available, is_featured, preparation_time, spicy_level,
    ingredients, allergens, calories, serving_size,
    rating, review_count, order_count,
    created_at, updated_at, category_id
) VALUES
-- 1. APPETIZERS
('Garlic Bread', 'garlic-bread', 'Toasted bread with garlic butter and herbs.',
 35000.00, '/dishes/dish1.jpg', TRUE, FALSE, 10, 0,
 'Bread, garlic, butter, parsley', 'Gluten, dairy', 250, '2 slices',
 4.5, 10, 50, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'appetizers')),

('Bruschetta', 'bruschetta', 'Grilled bread with tomato and basil.',
 45000.00, '/dishes/dish2.jpg', TRUE, TRUE, 12, 0,
 'Bread, tomato, basil, olive oil', 'Gluten', 180, '3 pieces',
 4.7, 8, 40, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'appetizers')),

-- 2. MAIN COURSE
('Margherita Pizza', 'margherita-pizza', 'Classic pizza with tomato, mozzarella, and basil.',
 129000.00, '/dishes/dish3.jpg', TRUE, TRUE, 20, 1,
 'Flour, tomato, mozzarella, basil', 'Gluten, dairy', 900, '1 pizza',
 4.8, 25, 120, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'main-course')),

('Grilled Chicken', 'grilled-chicken', 'Herb-marinated grilled chicken breast.',
 135000.00, '/dishes/dish4.jpg', TRUE, FALSE, 18, 1,
 'Chicken, herbs, olive oil', NULL, 350, '250 g',
 4.6, 15, 70, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'main-course')),

-- 3. BEVERAGES
('Lemon Iced Tea', 'lemon-iced-tea', 'Freshly brewed iced tea with lemon.',
 25000.00, '/dishes/dish5.jpg', TRUE, FALSE, 5, 0,
 'Black tea, lemon, sugar, ice', NULL, 90, '300 ml glass',
 4.2, 8, 40, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'beverages')),

('Espresso', 'espresso', 'Single shot of rich espresso.',
 20000.00, '/dishes/dish6.jpg', TRUE, FALSE, 3, 0,
 'Coffee beans, water', NULL, 5, '60 ml',
 4.9, 30, 200, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'beverages')),

-- 4. DESSERTS
('Chocolate Cake', 'chocolate-cake', 'Moist chocolate cake with ganache.',
 55000.00, '/dishes/dish7.jpg', TRUE, TRUE, 15, 0,
 'Flour, cocoa, sugar, eggs', 'Gluten, eggs', 420, '1 slice',
 4.9, 20, 80, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'desserts')),

('Cheesecake', 'cheesecake', 'Classic baked cheesecake.',
 60000.00, '/dishes/dish8.jpg', TRUE, FALSE, 15, 0,
 'Cream cheese, sugar, eggs, crust', 'Gluten, dairy, eggs', 450, '1 slice',
 4.7, 12, 60, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'desserts')),

-- 5. VEGETARIAN
('Caesar Salad', 'caesar-salad', 'Romaine lettuce with Caesar dressing and croutons.',
 75000.00, '/dishes/dish9.jpg', TRUE, FALSE, 10, 0,
 'Lettuce, croutons, parmesan, dressing', 'Gluten, dairy', 320, '1 bowl',
 4.3, 9, 45, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'vegetarian')),

('Greek Salad', 'greek-salad', 'Tomato, cucumber, feta, and olives.',
 85000.00, '/dishes/dish10.jpg', TRUE, FALSE, 10, 0,
 'Tomato, cucumber, feta, olives', 'Dairy', 280, '1 bowl',
 4.4, 7, 38, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'vegetarian')),

('Tomato Soup', 'tomato-soup', 'Creamy tomato soup with basil.',
 45000.00, '/dishes/dish11.jpg', TRUE, FALSE, 12, 0,
 'Tomato, cream, basil', 'Dairy', 150, '1 bowl',
 4.1, 5, 25, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'vegetarian')),

('Mushroom Soup', 'mushroom-soup', 'Creamy mushroom soup.',
 45000.00, '/dishes/dish12.jpg', TRUE, FALSE, 12, 0,
 'Mushroom, cream, herbs', 'Dairy', 180, '1 bowl',
 4.2, 6, 28, NOW(), NOW(), (SELECT id FROM categories WHERE slug = 'vegetarian'));

-- =========================================================
-- 4. DINING TABLES
-- =========================================================
INSERT INTO dining_table (name, floor, capacity, status)
VALUES
    -- Floor 1 Tables
    ('T01', 'F1', 2, 'OPEN'),
    ('T02', 'F1', 2, 'OPEN'),
    ('T03', 'F1', 2, 'OPEN'),
    ('T04', 'F1', 2, 'OPEN'),
    ('T05', 'F1', 2, 'OPEN'),
    ('T06', 'F1', 2, 'OPEN'),
    ('T07', 'F1', 2, 'OPEN'),
    ('T08', 'F1', 2, 'OPEN'),
    ('T09', 'F1', 2, 'OPEN'),
    ('T10', 'F1', 2, 'OPEN'),
    ('T11', 'F1', 2, 'OPEN'),
    ('T12', 'F1', 2, 'OPEN'),
    ('T13', 'F1', 2, 'OPEN'),
    ('T14', 'F1', 2, 'OPEN'),
    ('T15', 'F1', 2, 'OPEN'),
    ('T16', 'F1', 2, 'OPEN'),
    ('T17', 'F1', 4, 'OPEN'),
    ('T18', 'F1', 4, 'OPEN'),
    ('T19', 'F1', 4, 'OPEN'),
    ('T20', 'F1', 4, 'OPEN'),
    ('T21', 'F1', 4, 'OPEN'),
    ('T22', 'F1', 4, 'OPEN'),
    ('T23', 'F1', 4, 'OPEN'),
    ('T24', 'F1', 4, 'OPEN'),

-- Floor 2 Tables
    ('T25', 'F2', 4, 'OPEN'),
    ('T26', 'F2', 4, 'OPEN'),
    ('T27', 'F2', 4, 'OPEN'),
    ('T28', 'F2', 4, 'OPEN'),
    ('T29', 'F2', 4, 'OPEN'),
    ('T30', 'F2', 4, 'OPEN'),
    ('T31', 'F2', 4, 'OPEN'),
    ('T32', 'F2', 4, 'OPEN'),
    ('T33', 'F2', 4, 'OPEN'),
    ('T34', 'F2', 4, 'OPEN'),
    ('T35', 'F2', 4, 'OPEN'),
    ('T36', 'F2', 4, 'OPEN'),
    ('T37', 'F2', 8, 'OPEN'),
    ('T38', 'F2', 8, 'OPEN'),
    ('T39', 'F2', 8, 'OPEN'),
    ('T40', 'F2', 8, 'OPEN'),
    ('T41', 'F2', 8, 'OPEN'),
    ('T42', 'F2', 8, 'OPEN'),
    ('T43', 'F2', 8, 'OPEN'),
    ('T44', 'F2', 8, 'OPEN'),

-- Floor 3 Tables
    ('T45', 'F3', 8, 'OPEN'),
    ('T46', 'F3', 8, 'OPEN'),
    ('T47', 'F3', 8, 'OPEN'),
    ('T48', 'F3', 8, 'OPEN'),
    ('T49', 'F3', 8, 'OPEN'),
    ('T50', 'F3', 8, 'OPEN'),
    ('T51', 'F3', 8, 'OPEN'),
    ('T52', 'F3', 8, 'OPEN'),
    ('T53', 'F3', 10, 'OPEN'),
    ('T54', 'F3', 10, 'OPEN'),
    ('T55', 'F3', 10, 'OPEN'),
    ('T56', 'F3', 10, 'OPEN'),
    ('T57', 'F3', 10, 'OPEN');
