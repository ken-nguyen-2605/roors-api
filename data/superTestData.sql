TRUNCATE TABLE
    categories,
    menu_items,
    dining_table,
    reservation,
    orders,
    order_items
    RESTART IDENTITY CASCADE;

-- =========================================================
-- CATEGORIES (7 rows, NEW)
-- =========================================================
INSERT INTO categories (name, slug, description, image_url, display_order, is_active, created_at, updated_at) VALUES
                                                                                                                  ('Appetizers', 'appetizers', 'Start your meal with our delicious appetizers', '/dishes/dish1.jpg', 1, true, NOW(), NOW()),
                                                                                                                  ('Main Course', 'main-course', 'Hearty and satisfying main dishes', '/dishes/dish1.jpg', 2, true, NOW(), NOW()),
                                                                                                                  ('Desserts', 'desserts', 'Sweet endings to your perfect meal', '/dishes/dish1.jpg', 3, true, NOW(), NOW()),
                                                                                                                  ('Beverages', 'beverages', 'Refreshing drinks to complement your meal', '/dishes/dish1.jpg', 4, true, NOW(), NOW()),
                                                                                                                  ('Vegetarian', 'vegetarian', 'Plant-based delicious options', '/dishes/dish1.jpg', 5, true, NOW(), NOW()),
                                                                                                                  ('Seafood', 'seafood', 'Fresh catches from the ocean', '/dishes/dish1.jpg', 6, true, NOW(), NOW()),
                                                                                                                  ('Miscellaneous', 'miscellaneous', 'Other items and fees', '/dishes/dish1.jpg', 7, true, NOW(), NOW());

-- =========================================================
-- MENU ITEMS (15 rows, UPDATED)
--  - image_url uses /dishes/dish{i}.jpg (i in 1..12, then reused)
--  - category slugs mapped to new categories
-- =========================================================
INSERT INTO menu_items (
    name, slug, description, price, image_url,
    is_available, is_featured, preparation_time, spicy_level,
    ingredients, allergens, calories, serving_size,
    rating, review_count, order_count,
    created_at, updated_at, category_id
) VALUES
      ('Garlic Bread', 'garlic-bread', 'Toasted bread with garlic butter and herbs.',
       15000.00, '/dishes/dish1.jpg', TRUE, FALSE, 10, 0,
       'Bread, garlic, butter, parsley', 'Gluten, dairy', 250, '2 slices',
       4.5, 10, 50,
       '2025-01-10 11:00:00', '2025-01-10 11:00:00',
       (SELECT id FROM categories WHERE slug = 'appetizers')),

      ('Bruschetta', 'bruschetta', 'Grilled bread with tomato and basil.',
       18000.00, '/dishes/dish2.jpg', TRUE, TRUE, 12, 0,
       'Bread, tomato, basil, olive oil', 'Gluten', 180, '3 pieces',
       4.7, 8, 40,
       '2025-01-10 11:01:00', '2025-01-10 11:01:00',
       (SELECT id FROM categories WHERE slug = 'appetizers')),

      ('Margherita Pizza', 'margherita-pizza', 'Classic pizza with tomato, mozzarella, and basil.',
       129000.00, '/dishes/dish3.jpg', TRUE, TRUE, 20, 1,
       'Flour, tomato, mozzarella, basil', 'Gluten, dairy', 900, '1 pizza',
       4.8, 25, 120,
       '2025-01-10 11:02:00', '2025-01-10 11:02:00',
       (SELECT id FROM categories WHERE slug = 'main-course')),

      ('Grilled Chicken', 'grilled-chicken', 'Herb-marinated grilled chicken breast.',
       135000.00, '/dishes/dish4.jpg', TRUE, FALSE, 18, 1,
       'Chicken, herbs, olive oil', NULL, 350, '250 g',
       4.6, 15, 70,
       '2025-01-10 11:03:00', '2025-01-10 11:03:00',
       (SELECT id FROM categories WHERE slug = 'main-course')),

      ('Lemon Iced Tea', 'lemon-iced-tea', 'Freshly brewed iced tea with lemon.',
       10000.00, '/dishes/dish5.jpg', TRUE, FALSE, 5, 0,
       'Black tea, lemon, sugar, ice', NULL, 90, '300 ml glass',
       4.2, 8, 40,
       '2025-01-10 11:04:00', '2025-01-10 11:04:00',
       (SELECT id FROM categories WHERE slug = 'beverages')),

      ('Espresso', 'espresso', 'Single shot of rich espresso.',
       10000.00, '/dishes/dish6.jpg', TRUE, FALSE, 3, 0,
       'Coffee beans, water', NULL, 5, '60 ml',
       4.9, 30, 200,
       '2025-01-10 11:05:00', '2025-01-10 11:05:00',
       (SELECT id FROM categories WHERE slug = 'beverages')),

      ('Chocolate Cake', 'chocolate-cake', 'Moist chocolate cake with ganache.',
       10000.00, '/dishes/dish7.jpg', TRUE, TRUE, 15, 0,
       'Flour, cocoa, sugar, eggs', 'Gluten, eggs', 420, '1 slice',
       4.9, 20, 80,
       '2025-01-10 11:06:00', '2025-01-10 11:06:00',
       (SELECT id FROM categories WHERE slug = 'desserts')),

      ('Cheesecake', 'cheesecake', 'Classic baked cheesecake.',
       10000.00, '/dishes/dish8.jpg', TRUE, FALSE, 15, 0,
       'Cream cheese, sugar, eggs, crust', 'Gluten, dairy, eggs', 450, '1 slice',
       4.7, 12, 60,
       '2025-01-10 11:07:00', '2025-01-10 11:07:00',
       (SELECT id FROM categories WHERE slug = 'desserts')),

      ('Caesar Salad', 'caesar-salad', 'Romaine lettuce with Caesar dressing.',
       10000.00, '/dishes/dish9.jpg', TRUE, FALSE, 10, 0,
       'Lettuce, croutons, parmesan, dressing', 'Gluten, dairy', 320, '1 bowl',
       4.3, 9, 45,
       '2025-01-10 11:08:00', '2025-01-10 11:08:00',
       (SELECT id FROM categories WHERE slug = 'vegetarian')),

      ('Greek Salad', 'greek-salad', 'Tomato, cucumber, feta, and olives.',
       10000.00, '/dishes/dish10.jpg', TRUE, FALSE, 10, 0,
       'Tomato, cucumber, feta, olives', 'Dairy', 280, '1 bowl',
       4.4, 7, 38,
       '2025-01-10 11:09:00', '2025-01-10 11:09:00',
       (SELECT id FROM categories WHERE slug = 'vegetarian')),

      ('Tomato Soup', 'tomato-soup', 'Creamy tomato soup with basil.',
       10000.00, '/dishes/dish11.jpg', TRUE, FALSE, 12, 0,
       'Tomato, cream, basil', 'Dairy', 150, '1 bowl',
       4.1, 5, 25,
       '2025-01-10 11:10:00', '2025-01-10 11:10:00',
       (SELECT id FROM categories WHERE slug = 'vegetarian')),

      ('Mushroom Soup', 'mushroom-soup', 'Creamy mushroom soup.',
       10000.00, '/dishes/dish12.jpg', TRUE, FALSE, 12, 0,
       'Mushroom, cream, herbs', 'Dairy', 180, '1 bowl',
       4.2, 6, 28,
       '2025-01-10 11:11:00', '2025-01-10 11:11:00',
       (SELECT id FROM categories WHERE slug = 'vegetarian'));


-- =========================================================
-- DINING TABLES (15 rows)
-- (unchanged)
-- =========================================================
INSERT INTO dining_table (name, floor, capacity, status)
VALUES
    -- Floor 1 Tables
    ('T01', 'F1', 2, 0),
    ('T02', 'F1', 2, 0),
    ('T03', 'F1', 2, 0),
    ('T04', 'F1', 2, 0),
    ('T05', 'F1', 2, 0),
    ('T06', 'F1', 2, 0),
    ('T07', 'F1', 2, 0),
    ('T08', 'F1', 2, 0),
    ('T09', 'F1', 2, 0),
    ('T10', 'F1', 2, 0),
    ('T11', 'F1', 2, 0),
    ('T12', 'F1', 2, 0),
    ('T13', 'F1', 2, 0),
    ('T14', 'F1', 2, 0),
    ('T15', 'F1', 2, 0),
    ('T16', 'F1', 2, 0),
    ('T17', 'F1', 4, 0),
    ('T18', 'F1', 4, 0),
    ('T19', 'F1', 4, 0),
    ('T20', 'F1', 4, 0),
    ('T21', 'F1', 4, 0),
    ('T22', 'F1', 4, 0),
    ('T23', 'F1', 4, 0),
    ('T24', 'F1', 4, 0),

-- Floor 2 Tables
    ('T25', 'F2', 4, 0),
    ('T26', 'F2', 4, 0),
    ('T27', 'F2', 4, 0),
    ('T28', 'F2', 4, 0),
    ('T29', 'F2', 4, 0),
    ('T30', 'F2', 4, 0),
    ('T31', 'F2', 4, 0),
    ('T32', 'F2', 4, 0),
    ('T33', 'F2', 4, 0),
    ('T34', 'F2', 4, 0),
    ('T35', 'F2', 4, 0),
    ('T36', 'F2', 4, 0),
    ('T37', 'F2', 8, 0),
    ('T38', 'F2', 8, 0),
    ('T39', 'F2', 8, 0),
    ('T40', 'F2', 8, 0),
    ('T41', 'F2', 8, 0),
    ('T42', 'F2', 8, 0),
    ('T43', 'F2', 8, 0),
    ('T44', 'F2', 8, 0),

-- Floor 3 Tables
    ('T45', 'F3', 8, 0),
    ('T46', 'F3', 8, 0),
    ('T47', 'F3', 8, 0),
    ('T48', 'F3', 8, 0),
    ('T49', 'F3', 8, 0),
    ('T50', 'F3', 8, 0),
    ('T51', 'F3', 8, 0),
    ('T52', 'F3', 8, 0),
    ('T53', 'F3', 10, 0),
    ('T54', 'F3', 10, 0),
    ('T55', 'F3', 10, 0),
    ('T56', 'F3', 10, 0),
    ('T57', 'F3', 10, 0);

-- =========================================================
-- RESERVATIONS (15 rows) – user_id fixed 253
-- (unchanged)
-- =========================================================
INSERT INTO reservation (
    user_id, dining_table_id, status,
    phone, number_of_guests, start_time, end_time
) VALUES
      (253, (SELECT id FROM dining_table WHERE name='T01'),  0, '0900000001', 2, '2025-11-13 18:00:00', '2025-11-13 19:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T02'),  2, '0900000002', 2, '2025-11-13 18:30:00', '2025-11-13 20:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T03'),  1, '0900000003', 2, '2025-11-13 19:00:00', '2025-11-13 20:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T04'),  0, '0900000004', 2, '2025-11-13 19:30:00', '2025-11-13 21:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T05'),  3, '0900000005', 2, '2025-11-13 20:00:00', '2025-11-13 21:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T06'),  0, '0900000006', 2, '2025-11-14 18:00:00', '2025-11-14 19:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T07'),  2, '0900000007', 2, '2025-11-14 18:30:00', '2025-11-14 20:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T08'),  0, '0900000008', 2, '2025-11-14 19:00:00', '2025-11-14 20:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T09'),  1, '0900000009', 2, '2025-11-14 19:30:00', '2025-11-14 21:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T10'), 3, '0900000010', 2, '2025-11-14 20:00:00', '2025-11-14 21:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T11'), 0, '0900000011', 2, '2025-11-15 18:00:00', '2025-11-15 19:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T12'), 2, '0900000012', 2, '2025-11-15 18:30:00', '2025-11-15 20:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T13'), 0, '0900000013', 2, '2025-11-15 19:00:00', '2025-11-15 20:30:00'),
      (253, (SELECT id FROM dining_table WHERE name='T14'), 1, '0900000014', 2, '2025-11-15 19:30:00', '2025-11-15 21:00:00'),
      (253, (SELECT id FROM dining_table WHERE name='T15'), 3, '0900000015', 2, '2025-11-15 20:00:00', '2025-11-15 21:30:00');

-- =========================================================
-- ORDERS (15 rows) – user_id fixed 253
-- (unchanged)
-- =========================================================
INSERT INTO orders (
    id, user_id, order_number, status, order_type,
    subtotal, tax_amount, delivery_fee, discount_amount, total_amount,
    customer_name, customer_phone, customer_email, delivery_address,
    special_instructions, table_number, estimated_preparation_time,
    created_at, updated_at,
    -- NEW COLUMNS (10)
    preparation_started_at, ready_at, completed_at, cancelled_at,
    cancellation_reason, rating, feedback, rated_at, admin_response, responded_at
) VALUES
-- Corresponds to order_id 2001 (PREPARING) - 19 old values + 10 new
(2001, 253, 'ORD2001', 'PREPARING', 'DELIVERY',
 48.75, 4.88, 2.00, 0.00, 55.63,
 'Alice Nguyen', '0901000001', 'alice1@example.com', 'Street 1, City', 'No onions', NULL, 25,
 '2025-11-12 18:37:00', '2025-11-12 18:38:00',
 '2025-11-12 18:38:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2002 (COMPLETED) - 19 old values + 10 new
(2002, 253, 'ORD2002', 'COMPLETED', 'DELIVERY',
 89.20, 8.92, 2.00, 0.00, 100.12,
 'Bob Tran', '0901000002', 'bob2@example.com', 'Street 2, City', 'Extra sauce', NULL, 30,
 '2025-11-10 19:47:00', '2025-11-10 21:15:00',
 '2025-11-10 19:50:00', '2025-11-10 20:30:00', '2025-11-10 21:15:00', NULL, NULL, 5, 'Great food!', '2025-11-10 21:30:00', 'Thank you!', '2025-11-10 21:35:00'),

-- Corresponds to order_id 2003 (PENDING) - 19 old values + 10 new
(2003, 253, 'ORD2003', 'PENDING', 'DELIVERY',
 22.50, 2.25, 2.00, 0.00, 26.75,
 'Charlie Le', '0901000003', 'charlie3@example.com', 'Street 3, City', 'Light ice', NULL, 20,
 '2025-11-11 12:31:30', '2025-11-11 12:31:30',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2004 (READY) - 19 old values + 10 new
(2004, 253, 'ORD2004', 'READY', 'DELIVERY',
 57.00, 5.70, 2.00, 0.00, 64.70,
 'Dung Vo', '0901000004', 'dung4@example.com', 'Street 4, City', 'Leave at reception', NULL, 30,
 '2025-11-12 19:06:30', '2025-11-12 19:20:00',
 '2025-11-12 19:07:00', '2025-11-12 19:20:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2005 (COMPLETED) - 19 old values + 10 new
(2005, 253, 'ORD2005', 'COMPLETED', 'DELIVERY',
 107.00, 10.70, 2.00, 0.00, 119.70,
 'Emma Pham', '0901000005', 'emma5@example.com', 'Street 5, City', 'Birthday plate', NULL, 15,
 '2025-11-09 18:47:00', '2025-11-09 20:30:00',
 '2025-11-09 18:50:00', '2025-11-09 19:10:00', '2025-11-09 20:30:00', NULL, NULL, 4, 'Good, but a bit slow.', '2025-11-09 20:45:00', NULL, NULL),

-- Corresponds to order_id 2006 (CANCELLED) - 19 old values + 10 new
(2006, 253, 'ORD2006', 'CANCELLED', 'DELIVERY',
 41.25, 4.13, 2.00, 0.00, 47.38,
 'Fred Ho', '0901000006', 'fred6@example.com', 'Street 6, City', 'Service fee reversal', NULL, 25,
 '2025-11-08 20:02:00', '2025-11-08 20:10:00',
 NULL, NULL, NULL, '2025-11-08 20:10:00', 'Customer request', NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2007 (COMPLETED) - 19 old values + 10 new
(2007, 253, 'ORD2007', 'COMPLETED', 'DELIVERY',
 76.80, 7.68, 2.00, 0.00, 86.48,
 'Giang Do', '0901000007', 'giang7@example.com', 'Street 7, City', 'Gate delivery', NULL, 15,
 '2025-11-07 17:56:30', '2025-11-07 19:00:00',
 '2025-11-07 18:00:00', '2025-11-07 18:30:00', '2025-11-07 19:00:00', NULL, NULL, 2, 'Food was cold.', '2025-11-07 19:15:00', 'We are sorry!', '2025-11-07 19:20:00'),

-- Corresponds to order_id 2008 (COMPLETED) - 19 old values + 10 new
(2008, 253, 'ORD2008', 'COMPLETED', 'DELIVERY',
 33.10, 3.31, 0.00, 0.00, 36.41,
 'Hoa Vu', '0901000008', 'hoa8@example.com', NULL, 'Pickup by 13:00', NULL, 30,
 '2025-11-06 12:46:30', '2025-11-06 13:30:00',
 '2025-11-06 12:50:00', '2025-11-06 13:20:00', '2025-11-06 13:30:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2009 (PREPARING) - 19 old values + 10 new
(2009, 253, 'ORD2009', 'PREPARING', 'DELIVERY',
 102.90, 10.29, 0.00, 0.00, 113.19,
 'Ian Phan', '0901000009', 'ian9@example.com', NULL, 'Family set for 4', '12', 20,
 '2025-11-12 19:16:30', '2025-11-12 19:17:00',
 '2025-11-12 19:17:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

-- Corresponds to order_id 2010 (COMPLETED) - 19 old values + 10 new
(2010, 253, 'ORD2010', 'COMPLETED', 'DELIVERY',
 59.50, 5.95, 2.00, 0.00, 67.45,
 'Jane Ha', '0901000010', 'jane10@example.com', 'Street 10, City', 'Call on arrival', NULL, 25,
 '2025-11-01 18:01:30', '2025-11-01 19:00:00',
 '2025-11-01 18:05:00', '2025-11-01 18:30:00', '2025-11-01 19:00:00', NULL, NULL, 3, 'Just okay.', '2025-11-01 19:10:00', NULL, NULL);


-- =========================================================
-- ORDER ITEMS (15 rows)
-- (unchanged)
-- =========================================================

INSERT INTO order_items (
    id, order_id, menu_item_id, menu_item_name, unit_price, quantity,
    subtotal, special_instructions, created_at,
    dish_rating, dish_feedback, dish_rated_at, admin_dish_response, dish_responded_at
) VALUES

-- Order 2001 (Confirmed - No ratings)
-- ID 7 changed from 'Beef Burger Deluxe' (14.50) to 'Chocolate Cake' (5.50)
(3001, 2001, 7,  'Chocolate Cake', 5.50, 2, 11.00, NULL, '2025-11-12 18:36:00', NULL, NULL, NULL, NULL, NULL),
-- ID 17 ('Iced Caramel Latte') does not exist in your menu. Item removed.

-- Order 2002 (Completed - Rated highly)
-- ID 4 changed from 'Grilled Ribeye Steak' (24.50) to 'Grilled Chicken' (14.50)
(3003, 2002, 4,  'Grilled Chicken', 14.50, 2, 29.00, 'Extra sauce', '2025-11-10 19:46:00', 5, 'Perfectly cooked medium rare.', '2025-11-10 21:15:00', 'Glad you liked it!', '2025-11-10 22:00:00'),
-- ID 6 changed from 'BBQ Ribs' (40.20) to 'Espresso' (2.00)
(3004, 2002, 6,  'Espresso', 2.00, 1, 2.00, NULL, '2025-11-10 19:47:00', 5, 'Very tender.', '2025-11-10 21:15:00', NULL, NULL),

-- Order 2003 (Pending - No ratings)
-- ID 12 changed from 'Margherita Pizza' (12.50) to 'Mushroom Soup' (6.50)
(3005, 2003, 12, 'Mushroom Soup', 6.50, 1, 6.50, NULL, '2025-11-11 12:31:00', NULL, NULL, NULL, NULL, NULL),
-- ID 16 ('Fresh Lemonade') does not exist in your menu. Item removed.

-- Order 2004 (Confirmed - No ratings)
-- ID 8 changed from 'Grilled Salmon' (27.00) to 'Cheesecake' (5.75)
(3007, 2004, 8,  'Cheesecake', 5.75, 1, 5.75, 'Leave at reception', '2025-11-12 19:06:00', NULL, NULL, NULL, NULL, NULL),
-- ID 9 changed from 'Shrimp Scampi' (15.00) to 'Caesar Salad' (8.00)
(3008, 2004, 9,  'Caesar Salad', 8.00, 2, 16.00, NULL, '2025-11-12 19:06:30', NULL, NULL, NULL, NULL, NULL),

-- Order 2005 (Completed - Mixed ratings)
-- ID 13 ('Chocolate Lava Cake') does not exist in your menu. Item removed.
-- ID 14 ('New York Cheesecake') does not exist in your menu. Item removed.

-- Order 2006 (Cancelled - No ratings)
-- ID 3 changed from 'Mozzarella Sticks' (20.00) to 'Margherita Pizza' (12.00)
(3011, 2006, 3,  'Margherita Pizza', 12.00, 2, 24.00, NULL, '2025-11-08 20:01:00', NULL, NULL, NULL, NULL, NULL),
-- ID 19 ('Service Fee') does not exist in your menu. Item removed.

-- Order 2007 (Completed - Bad ratings)
-- ID 11 changed from 'Vegetable Stir Fry' (18.90) to 'Tomato Soup' (6.00)
(3013, 2007, 11, 'Tomato Soup', 6.00, 2, 12.00, NULL, '2025-11-07 17:56:00', 1, 'Soggy vegetables.', '2025-11-07 19:00:00', NULL, NULL),
-- ID 4 changed from 'Grilled Ribeye Steak' (39.00) to 'Grilled Chicken' (14.50)
(3014, 2007, 4,  'Grilled Chicken', 14.50, 1, 14.50, 'Gate delivery', '2025-11-07 17:56:30', 2, 'Cold when arrived.', '2025-11-07 19:00:00', NULL, NULL),

-- Order 2008 (Completed - Good ratings, no text)
-- ID 10 changed from 'Mushroom Risotto' (13.50) to 'Greek Salad' (8.50)
(3015, 2008, 10, 'Greek Salad', 8.50, 1, 8.50, NULL, '2025-11-06 12:46:00', 5, NULL, '2025-11-06 13:30:00', NULL, NULL),
-- ID 16 ('Fresh Lemonade') does not exist in your menu. Item removed.

-- Order 2009 (Confirmed - No ratings)
-- ID 4 changed from 'Grilled Ribeye Steak' (25.00) to 'Grilled Chicken' (14.50)
(3017, 2009, 4,  'Grilled Chicken', 14.50, 4, 58.00, 'Family set for 4', '2025-11-12 19:16:00', NULL, NULL, NULL, NULL, NULL),
-- ID 20 ('Sauce Add-on') does not exist in your menu. Item removed.

-- Order 2010 (Completed - Average rating)
-- ID 5 changed from 'Chicken Parmesan' (30.00) to 'Lemon Iced Tea' (2.75)
(3019, 2010, 5,  'Lemon Iced Tea', 2.75, 1, 2.75, 'Call on arrival', '2025-11-01 18:01:00', 3, 'Chicken was dry.', '2025-11-01 19:00:00', NULL, NULL);
-- ID 17 ('Iced Caramel Latte') does not exist in your menu. Item removed.


--  ((SELECT id FROM orders WHERE order_number='ORD1013'),
--  (SELECT id FROM menu_items WHERE slug='grilled-chicken'),
--  'Grilled Chicken', 14.50, 2, 29.00, 'Extra sauce.', '2025-11-14 20:02:00');