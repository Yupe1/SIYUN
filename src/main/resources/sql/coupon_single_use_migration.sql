-- 同一前台用户对同一张优惠券只能持有一条领取记录。
-- 执行前如存在历史重复数据，请先保留最早的一条并处理其余记录。
ALTER TABLE op_coupon_user
    ADD UNIQUE KEY uk_coupon_user_once (coupon_id, user_id);

-- 一笔订单最多关联并核销一张优惠券；MySQL 唯一索引允许多条 NULL。
ALTER TABLE op_coupon_user
    ADD UNIQUE KEY uk_coupon_user_order_once (order_id);
