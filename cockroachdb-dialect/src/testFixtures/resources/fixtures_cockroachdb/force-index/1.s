CREATE TABLE users (
  id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  age INT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_email (email),
  INDEX idx_age (age)
);

-- Orders table used by the JOIN in the fixtures. Includes an index `idx_user_id` referenced in the FORCE_INDEX hint.
CREATE TABLE orders (
  id INT NOT NULL,
  user_id INT NOT NULL,
  product VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id)
);

-- Force index simple usage
SELECT * FROM users@idx_age WHERE age > 30;

-- Force index usage
SELECT * FROM users@{FORCE_INDEX = idx_email} WHERE email = 'test@example.com';

-- Force index with alias
SELECT u.* FROM users@{FORCE_INDEX = idx_age} u WHERE u.age > 18;

-- Multiple force index hints in join
SELECT u.*, o.*
FROM users@{FORCE_INDEX = idx_email} u
JOIN orders@{FORCE_INDEX = idx_user_id} o ON u.id = o.user_id
WHERE u.email = 'test@example.com';

-- Force zigzag join
SELECT * FROM users@{FORCE_ZIGZAG = idx_email} WHERE email = 'test@example.com';

-- Force zigzag with multiple parameters
SELECT * FROM users@{FORCE_ZIGZAG = idx_age} WHERE email LIKE 'A%' AND age > 5;

-- Force index + No full scan
SELECT * FROM users@{FORCE_INDEX = idx_email,NO_FULL_SCAN} WHERE email = 'test@example.com';

-- Force index + Avoid full scan
SELECT * FROM users@{FORCE_INDEX = idx_email,AVOID_FULL_SCAN} WHERE email = 'test@example.com';

-- No zigzag join
SELECT * FROM users@{NO_ZIGZAG_JOIN} WHERE email = 'test@example.com';

-- No index join
SELECT * FROM users@{NO_INDEX_JOIN} WHERE email = 'test@example.com';
