-- 修改数据库字符集
ALTER DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改user表字符集
ALTER TABLE blog_db.user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 特别修改nickname字段的字符集
ALTER TABLE blog_db.user MODIFY nickname VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;