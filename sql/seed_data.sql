-- =========================
-- 0) 초기화 (FK 끊고 TRUNCATE)
-- =========================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE comments;
TRUNCATE TABLE posts;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 1) numbers 테이블 생성 (1 ~ 100,000)
-- =========================
DROP TABLE IF EXISTS numbers;
CREATE TABLE numbers (
  n INT NOT NULL PRIMARY KEY
) ENGINE=InnoDB;

INSERT INTO numbers (n)
SELECT a.i + b.i*10 + c.i*100 + d.i*1000 + e.i*10000 + 1 AS n
FROM (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b
CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e;


-- =========================
-- 2) users 10000
-- =========================
INSERT INTO users (username, email, password, nickname)
SELECT
  CONCAT('user', n),
  CONCAT('user', n, '@test.com'),
  '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890abcdef', -- 더미 bcrypt 해시
  CONCAT('user_', n)
FROM numbers
WHERE n <= 10000;

-- =========================
-- 3) posts 100,000
-- =========================
INSERT INTO posts (user_id, title, content, view_count, created_at)
SELECT
  ((n - 1) % 10000) + 1 AS user_id,
  CONCAT('게시글 제목 ', n),
  CONCAT('게시글 내용 ', n),
  n % 500 AS view_count,
  NOW() - INTERVAL (n % 365) DAY AS created_at
FROM numbers
WHERE n <= 100000;

-- =========================
-- 4) comments 100,000
-- =========================
INSERT INTO comments (post_id, user_id, content, created_at)
SELECT
  ((n - 1) % 100000) + 1 AS post_id,
  ((n - 1) % 10000) + 1 AS user_id,
  CONCAT('댓글 내용 ', n),
  NOW() - INTERVAL (n % 365) DAY AS created_at
FROM numbers
WHERE n <= 100000;

-- posts 테이블 comments_cnt 동기화
SET SQL_SAFE_UPDATES = 0;

UPDATE posts p
LEFT JOIN (
  SELECT post_id, COUNT(*) AS cnt
  FROM comments
  GROUP BY post_id
) c ON c.post_id = p.id
SET p.comments_cnt = COALESCE(c.cnt, 0);

SET SQL_SAFE_UPDATES = 1;

-- (선택) 확인
SELECT (SELECT COUNT(*) FROM users) AS users_cnt,
       (SELECT COUNT(*) FROM posts) AS posts_cnt,
       (SELECT COUNT(*) FROM comments) AS comments_cnt;