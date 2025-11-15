-- 修改article表的字符集配置
ALTER TABLE blog_db.article CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 确保单个字段也正确设置
ALTER TABLE blog_db.article MODIFY title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE blog_db.article MODIFY content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE blog_db.article MODIFY cover_image VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;

-- 显示修改后的表结构
SHOW CREATE TABLE blog_db.article;