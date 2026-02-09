SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_profiles;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- users
-- =========================
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 PK',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 아이디',
    email VARCHAR(100) UNIQUE COMMENT '이메일 (선택)',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
    nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일'
);

-- =========================
-- posts
-- =========================
CREATE TABLE `posts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '게시글 PK',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '작성자 ID',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '제목',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '내용',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '조회수',
  `comments_cnt` int unsigned NOT NULL DEFAULT '0' COMMENT '댓글수',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `idx_posts_user_id` (`user_id`),
  KEY `idx_posts_created_at` (`created_at`),
  CONSTRAINT `fk_users_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
);

-- =========================
-- comments
-- =========================
CREATE TABLE `comments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '댓글 PK',
  `post_id` bigint unsigned DEFAULT NULL COMMENT '게시글 ID',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '댓글 작성자 ID',
  `content` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '댓글 내용',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `idx_comments_post_id` (`post_id`),
  KEY `idx_comments_user_id` (`user_id`),
  CONSTRAINT `fk_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
);