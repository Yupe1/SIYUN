-- 前台个人中心：创作者视频动态和钱包基础表。
ALTER TABLE js_moments_article
    ADD COLUMN video_url VARCHAR(500) NULL COMMENT '创作者上传的视频地址' AFTER cover_url;

CREATE TABLE IF NOT EXISTS op_front_user_wallet (
    front_user_id INT NOT NULL COMMENT '前台用户ID',
    wallet DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (front_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前台用户钱包';
