-- 修改article表title字段的字符集为utf8mb4
ALTER TABLE blog_db.article MODIFY COLUMN title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- 可选：修改整个表的默认字符集
ALTER TABLE blog_db.article CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;