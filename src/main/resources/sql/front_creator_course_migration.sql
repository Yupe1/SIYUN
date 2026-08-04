-- 前台创作者视频课程：与后台教师课程共用课程/分集表，但使用独立的前台用户归属字段。
ALTER TABLE js_course
    MODIFY COLUMN teacher_id INT NULL COMMENT '主讲教师(后台用户ID，前台创作者课程为空)',
    ADD COLUMN front_creator_id INT NULL COMMENT '前台创作者用户ID' AFTER teacher_id,
    ADD INDEX idx_course_front_creator (front_creator_id);

-- 课程视图同时支持后台教师和前台创作者；只有审核通过且已上架的课程面向用户展示。
CREATE OR REPLACE VIEW v_course AS
SELECT
    c.id AS id,
    COALESCE(NULLIF(fu.nick_name, ''), bu.name, '创作者') AS teacher_name,
    c.front_creator_id AS front_creator_id,
    cc.cate_name AS cate_name,
    c.recommend_type AS recommend_type,
    c.title AS title,
    c.intro AS intro,
    c.price_original AS price_original,
    GREATEST(c.price_original - IFNULL(cp.max_coupon_amount, 0), 0) AS price_promotion,
    c.keywords AS keywords,
    c.cover_url AS cover_url,
    c.video_url AS video_url,
    c.detail_desc AS detail_desc,
    c.episode_num AS episode_num,
    c.duration AS duration,
    IFNULL(l.count_like, 0) AS count_like,
    IFNULL(s.count_share, 0) AS count_share,
    IFNULL(co.count_collect, 0) AS count_collect,
    IFNULL(p.count_view, 0) AS count_view,
    IFNULL(o.sales_volume, 0) AS sales_volume,
    c.create_by AS create_by,
    c.create_time AS create_time,
    c.update_by AS update_by,
    c.update_time AS update_time
FROM js_course c
LEFT JOIN (
    SELECT course_id, COUNT(1) AS count_like
    FROM apx_course_like_log
    GROUP BY course_id
) l ON c.id = l.course_id
LEFT JOIN (
    SELECT course_id, COUNT(1) AS count_share
    FROM apx_course_share_log
    GROUP BY course_id
) s ON c.id = s.course_id
LEFT JOIN (
    SELECT course_id, COUNT(1) AS count_collect
    FROM apx_course_collect_log
    GROUP BY course_id
) co ON c.id = co.course_id
LEFT JOIN (
    SELECT course_id, COUNT(1) AS count_view
    FROM apx_course_play_log
    GROUP BY course_id
) p ON c.id = p.course_id
LEFT JOIN (
    SELECT entity_id AS course_id, COUNT(1) AS sales_volume
    FROM op_order
    WHERE entity_type = 1 AND status >= 1 AND status < 7
    GROUP BY entity_id
) o ON c.id = o.course_id
LEFT JOIN (
    SELECT cg.goods_id AS course_id, MAX(coupon.amount) AS max_coupon_amount
    FROM op_coupon coupon
    JOIN op_coupon_goods cg ON coupon.id = cg.coupon_id
    WHERE coupon.status_shelf = 1
      AND cg.quota > 0
      AND coupon.apply_type IN (0, 2)
      AND NOW() BETWEEN coupon.start_time AND coupon.end_time
    GROUP BY cg.goods_id
) cp ON cp.course_id = c.id
LEFT JOIN obj_back_user bu ON c.teacher_id = bu.id
LEFT JOIN obj_front_user fu ON c.front_creator_id = fu.id
JOIN js_course_category cc ON cc.id = c.cate_id
WHERE c.status_shelf = 1
  AND c.status_audit = 3;
