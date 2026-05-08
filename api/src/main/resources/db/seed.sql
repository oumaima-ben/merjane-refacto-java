-- Seed data for manual API testing (loaded by the 'dev' profile).
-- Mirrors the products used by the integration tests so the same scenarios
-- can be exercised against a running server.

INSERT INTO products (id, lead_time, available, type, name, expiry_date, season_start_date, season_end_date) VALUES
  (1, 15, 30, 'NORMAL',    'USB Cable',  NULL,                              NULL,                                NULL),
  (2, 10,  0, 'NORMAL',    'USB Dongle', NULL,                              NULL,                                NULL),
  (3, 15, 30, 'EXPIRABLE', 'Butter',     DATEADD('DAY',  26, CURRENT_DATE), NULL,                                NULL),
  (4, 90,  6, 'EXPIRABLE', 'Milk',       DATEADD('DAY',  -2, CURRENT_DATE), NULL,                                NULL),
  (5, 15, 30, 'SEASONAL',  'Watermelon', NULL,                              DATEADD('DAY',  -2, CURRENT_DATE),   DATEADD('DAY',  58, CURRENT_DATE)),
  (6, 15, 30, 'SEASONAL',  'Grapes',     NULL,                              DATEADD('DAY', 180, CURRENT_DATE),   DATEADD('DAY', 240, CURRENT_DATE));

INSERT INTO orders (id) VALUES (1);

INSERT INTO order_items (order_id, product_id) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6);
