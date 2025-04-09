CREATE TABLE IF NOT EXISTS item_access_log (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_id` BIGINT UNSIGNED NOT NULL COMMENT '关联商品ID',
    `member_id` VARCHAR(64) COMMENT '用户唯一标识（未登录用户为空）',
    `ip` VARCHAR(45) NOT NULL COMMENT '客户端IP地址',
    `user_agent` VARCHAR(512) COMMENT '客户端浏览器/设备标识',
    `referer` VARCHAR(1024) COMMENT '来源页面URL',
    `access_time` DATETIME NOT NULL COMMENT '访问发生时间（ISO8601格式）',
    `access_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数计数器',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX idx_item_access_time (`item_id`, `access_time`),
    INDEX idx_access_time (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品访问日志表';