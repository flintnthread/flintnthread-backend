-- Test SQL query for getting products by main category ID
-- This simulates the query used in ProductRepository.findByMainCategory

-- Test case: Get all products from main category ID 1 and its subcategories
SELECT DISTINCT p.* 
FROM products p
WHERE p.category_id = 1
OR p.category_id IN (
    SELECT c.id FROM categories c WHERE c.parent_id = 1
);

-- Test case: Check what subcategories exist under main category ID 1
SELECT id, category_name, parent_id 
FROM categories 
WHERE parent_id = 1;

-- Test case: Count products by category to verify the logic
SELECT 
    c.id as category_id,
    c.category_name,
    c.parent_id,
    COUNT(p.id) as product_count
FROM categories c
LEFT JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.category_name, c.parent_id
ORDER BY c.parent_id, c.id;
