SELECT
  u.id AS user_id,
  u.first_name || ' ' || u.last_name AS full_name,
  u.email AS email,
  COALESCE(a.city, 'Unknown') AS city,
  UPPER(a.country) AS country,
  COUNT(o.id) AS total_orders
FROM users u
LEFT JOIN addresses a ON u.id = a.user_id
INNER JOIN orders o ON u.id = o.user_id