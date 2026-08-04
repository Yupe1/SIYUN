-- 轮播图改为从课程中选择后，需要保存明确的课程关联。
ALTER TABLE op_circle_ad
    ADD COLUMN course_id INT NULL COMMENT '关联课程ID' AFTER id,
    ADD INDEX idx_circle_ad_course_id (course_id);

UPDATE qf_permission
SET name = '优惠券关联查询',
    perm_desc = '按商品或课程查询适用优惠券',
    path = '/marketing/coupon-goods',
    update_by = 1,
    update_time = NOW()
WHERE id = 606;

UPDATE qf_permission
SET path = '/marketing/coupon-grant',
    update_by = 1,
    update_time = NOW()
WHERE id = 607;
