-- 拆分赠送优惠券菜单，并增加商品/课程优惠券绑定权限。
UPDATE qf_permission
SET name = '优惠券关联查询',
    perm_desc = '按商品或课程查询适用优惠券',
    update_by = 1,
    update_time = NOW()
WHERE id = 606;

UPDATE qf_permission
SET name = '赠送优惠券',
    perm_desc = '单独给用户赠送优惠券',
    path = '/marketing/coupon-grant',
    update_by = 1,
    update_time = NOW()
WHERE id = 607;

INSERT INTO qf_permission
    (id, name, perm_desc, parent_id, path, perms, status, create_time, update_time, update_by)
VALUES
    (614, '商品/课程优惠券', '为指定商品或课程绑定优惠券', 600,
     '/marketing/coupon-goods/create', 'admin:coupon:goods:add', 1, NOW(), NOW(), 1);

INSERT INTO qf_role_permission (id, role_id, permission_id) VALUES
    (200614, 2, 614),
    (400614, 4, 614);
