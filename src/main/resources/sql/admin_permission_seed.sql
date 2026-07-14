-- SIYUN 后台管理角色/菜单/权限初始化脚本
-- 可重复执行：会更新固定 ID 的角色/权限，并重建四个基础角色的权限绑定。
INSERT INTO qf_role (id, role_name, role_key, sort_num, status, remark, create_by, create_time) VALUES
(1, '老师', 'TEACHER', 1, 1, '教师后台：管理自己的课程、课程评论和微圈内容', NULL, NOW()),
(2, '运营', 'OPERATOR', 2, 1, '运营后台：订单、商品、课程、优惠券、轮播图、评论与统计', NULL, NOW()),
(3, '人事', 'HR', 3, 1, '人事后台：用户、教师/员工、部门、创作者申请与账号状态', NULL, NOW()),
(4, '管理员', 'ADMIN', 0, 1, '超级管理员：拥有全部后台功能和角色权限分配能力', NULL, NOW())
ON DUPLICATE KEY UPDATE
role_name = VALUES(role_name),
role_key = VALUES(role_key),
sort_num = VALUES(sort_num),
status = VALUES(status),
remark = VALUES(remark);

INSERT INTO qf_permission (id, name, perm_desc, parent_id, path, perms, status, create_time, update_time, update_by) VALUES
(100, '系统首页', '后台首页统计入口', 0, '/dashboard', 'admin:dashboard', 1, NOW(), NOW(), 1),
(101, '首页统计', '查看订单、用户、待办与快捷入口统计', 100, '/dashboard', 'admin:dashboard:view', 1, NOW(), NOW(), 1),

(200, '课程管理', '课程、分类、分集、评论与审核', 0, '/courses', 'admin:course', 1, NOW(), NOW(), 1),
(201, '课程列表', '查询课程列表', 200, '/courses/list', 'admin:course:list', 1, NOW(), NOW(), 1),
(202, '课程详情', '查看课程详情、分集和评论', 200, '/courses/detail', 'admin:course:detail', 1, NOW(), NOW(), 1),
(203, '添加课程', '新增课程并提交审核', 200, '/courses/create', 'admin:course:add', 1, NOW(), NOW(), 1),
(204, '编辑课程', '编辑课程基础信息', 200, '/courses/edit', 'admin:course:update', 1, NOW(), NOW(), 1),
(205, '删除课程', '删除课程', 200, '/courses/delete', 'admin:course:delete', 1, NOW(), NOW(), 1),
(206, '课程审核', '审核课程是否通过', 200, '/courses/audit', 'admin:course:audit', 1, NOW(), NOW(), 1),
(207, '课程日志', '查看课程审核/操作日志', 200, '/courses/logs', 'admin:course:log', 1, NOW(), NOW(), 1),
(208, '课程导出', '导出课程数据', 200, '/courses/export', 'admin:course:export', 1, NOW(), NOW(), 1),
(209, '课程导入', '批量导入课程数据', 200, '/courses/import', 'admin:course:import', 1, NOW(), NOW(), 1),
(210, '课程分集', '维护课程视频分集', 200, '/courses/contents', 'admin:course:content', 1, NOW(), NOW(), 1),
(211, '课程评论', '查看课程评论', 200, '/courses/comments', 'admin:course:comment', 1, NOW(), NOW(), 1),
(212, '课程分类', '查看课程分类树', 200, '/courses/categories', 'admin:course:category', 1, NOW(), NOW(), 1),
(213, '添加课程分类', '新增课程分类', 200, '/courses/categories/create', 'admin:course:category:add', 1, NOW(), NOW(), 1),
(214, '编辑课程分类', '编辑课程分类', 200, '/courses/categories/edit', 'admin:course:category:update', 1, NOW(), NOW(), 1),
(215, '删除课程分类', '删除课程分类', 200, '/courses/categories/delete', 'admin:course:category:delete', 1, NOW(), NOW(), 1),

(300, '微圈管理', '微圈内容、评论与审核', 0, '/moments', 'admin:moment', 1, NOW(), NOW(), 1),
(301, '微圈列表', '查询微圈列表', 300, '/moments/list', 'admin:moment:list', 1, NOW(), NOW(), 1),
(302, '微圈详情', '查看微圈详情', 300, '/moments/detail', 'admin:moment:detail', 1, NOW(), NOW(), 1),
(303, '添加微圈', '后台发布微圈内容', 300, '/moments/create', 'admin:moment:add', 1, NOW(), NOW(), 1),
(304, '编辑微圈', '编辑微圈内容', 300, '/moments/edit', 'admin:moment:update', 1, NOW(), NOW(), 1),
(305, '删除微圈', '删除微圈内容', 300, '/moments/delete', 'admin:moment:delete', 1, NOW(), NOW(), 1),
(306, '微圈审核', '审核微圈内容', 300, '/moments/audit', 'admin:moment:audit', 1, NOW(), NOW(), 1),

(400, '商品管理', '商品、分类和商品审核', 0, '/goods', 'admin:goods', 1, NOW(), NOW(), 1),
(401, '商品列表', '查询商品列表', 400, '/goods/list', 'admin:goods:list', 1, NOW(), NOW(), 1),
(402, '商品详情', '查看商品详情', 400, '/goods/detail', 'admin:goods:detail', 1, NOW(), NOW(), 1),
(403, '添加商品', '新增商品', 400, '/goods/create', 'admin:goods:add', 1, NOW(), NOW(), 1),
(404, '编辑商品', '编辑商品信息', 400, '/goods/edit', 'admin:goods:update', 1, NOW(), NOW(), 1),
(405, '删除商品', '删除/下架商品', 400, '/goods/delete', 'admin:goods:delete', 1, NOW(), NOW(), 1),
(406, '商品审核', '审核商品', 400, '/goods/audit', 'admin:goods:audit', 1, NOW(), NOW(), 1),
(407, '商品分类', '查看商品分类', 400, '/goods/categories', 'admin:goods:category', 1, NOW(), NOW(), 1),
(408, '添加商品分类', '新增商品分类', 400, '/goods/categories/create', 'admin:goods:category:add', 1, NOW(), NOW(), 1),
(409, '编辑商品分类', '编辑商品分类', 400, '/goods/categories/edit', 'admin:goods:category:update', 1, NOW(), NOW(), 1),
(410, '删除商品分类', '删除商品分类', 400, '/goods/categories/delete', 'admin:goods:category:delete', 1, NOW(), NOW(), 1),

(500, '订单管理', '商品订单、课程订单和退货', 0, '/orders', 'admin:order', 1, NOW(), NOW(), 1),
(501, '订单列表', '查看商品/课程订单', 500, '/orders/list', 'admin:order:list', 1, NOW(), NOW(), 1),
(502, '订单状态', '更新订单状态和物流', 500, '/orders/status', 'admin:order:update', 1, NOW(), NOW(), 1),
(503, '退货列表', '查看退货订单', 500, '/orders/returns', 'admin:order:return', 1, NOW(), NOW(), 1),
(504, '退货审核', '审核退货通过或拒绝', 500, '/orders/returns/audit', 'admin:order:return:audit', 1, NOW(), NOW(), 1),

(600, '运营营销', '优惠券、用户优惠券和轮播图', 0, '/marketing', 'admin:marketing', 1, NOW(), NOW(), 1),
(601, '优惠券列表', '查看优惠券', 600, '/marketing/coupons', 'admin:coupon:list', 1, NOW(), NOW(), 1),
(602, '添加优惠券', '新增优惠券', 600, '/marketing/coupons/create', 'admin:coupon:add', 1, NOW(), NOW(), 1),
(603, '编辑优惠券', '编辑优惠券', 600, '/marketing/coupons/edit', 'admin:coupon:update', 1, NOW(), NOW(), 1),
(604, '删除优惠券', '删除优惠券', 600, '/marketing/coupons/delete', 'admin:coupon:delete', 1, NOW(), NOW(), 1),
(605, '优惠券上下线', '上线或下线优惠券', 600, '/marketing/coupons/status', 'admin:coupon:status', 1, NOW(), NOW(), 1),
(606, '用户优惠券', '查询用户拥有的优惠券', 600, '/marketing/coupon-users', 'admin:coupon:user', 1, NOW(), NOW(), 1),
(607, '赠送优惠券', '单独给用户赠送优惠券', 600, '/marketing/coupon-users/grant', 'admin:coupon:grant', 1, NOW(), NOW(), 1),
(608, '轮播图列表', '查看轮播图', 600, '/marketing/ads', 'admin:ad:list', 1, NOW(), NOW(), 1),
(609, '添加轮播图', '新增轮播图', 600, '/marketing/ads/create', 'admin:ad:add', 1, NOW(), NOW(), 1),
(610, '编辑轮播图', '编辑轮播图', 600, '/marketing/ads/edit', 'admin:ad:update', 1, NOW(), NOW(), 1),
(611, '删除轮播图', '删除轮播图', 600, '/marketing/ads/delete', 'admin:ad:delete', 1, NOW(), NOW(), 1),
(612, '轮播图上下线', '上线或下线轮播图', 600, '/marketing/ads/status', 'admin:ad:status', 1, NOW(), NOW(), 1),
(613, '轮播图置顶', '将轮播图放置首位', 600, '/marketing/ads/top', 'admin:ad:top', 1, NOW(), NOW(), 1),

(700, '人事管理', '用户、员工、教师、部门和申请审核', 0, '/hr', 'admin:hr', 1, NOW(), NOW(), 1),
(701, '前台用户', '查看学生/前台用户', 700, '/hr/front-users', 'admin:user:list', 1, NOW(), NOW(), 1),
(702, '编辑前台用户', '编辑学生/前台用户信息', 700, '/hr/front-users/edit', 'admin:user:update', 1, NOW(), NOW(), 1),
(703, '账号封停', '封停、解封或注销账号', 700, '/hr/front-users/lock', 'admin:user:lock', 1, NOW(), NOW(), 1),
(704, '后台人员', '查看教师/运营/人事/管理员账号', 700, '/hr/staff-users', 'admin:staff:list', 1, NOW(), NOW(), 1),
(705, '添加后台人员', '新增教师或后台人员', 700, '/hr/staff-users/create', 'admin:staff:add', 1, NOW(), NOW(), 1),
(706, '编辑后台人员', '编辑教师或后台人员', 700, '/hr/staff-users/edit', 'admin:staff:update', 1, NOW(), NOW(), 1),
(707, '删除后台人员', '删除教师或后台人员', 700, '/hr/staff-users/delete', 'admin:staff:delete', 1, NOW(), NOW(), 1),
(708, '分配人员角色', '给后台人员分配角色', 700, '/hr/staff-users/roles', 'admin:staff:assign', 1, NOW(), NOW(), 1),
(709, '部门列表', '查看部门树', 700, '/hr/depts', 'admin:dept:list', 1, NOW(), NOW(), 1),
(710, '添加部门', '新增部门', 700, '/hr/depts/create', 'admin:dept:add', 1, NOW(), NOW(), 1),
(711, '编辑部门', '编辑部门', 700, '/hr/depts/edit', 'admin:dept:update', 1, NOW(), NOW(), 1),
(712, '删除部门', '删除部门', 700, '/hr/depts/delete', 'admin:dept:delete', 1, NOW(), NOW(), 1),
(713, '创作者申请', '查看作品/创作者申请', 700, '/hr/position-applies', 'admin:apply:list', 1, NOW(), NOW(), 1),
(714, '申请审核', '审核或驳回作品/创作者申请', 700, '/hr/position-applies/audit', 'admin:apply:audit', 1, NOW(), NOW(), 1),

(800, '权限管理', '角色、权限和菜单', 0, '/system', 'admin:system', 1, NOW(), NOW(), 1),
(801, '角色列表', '查看角色', 800, '/system/roles', 'admin:role:list', 1, NOW(), NOW(), 1),
(802, '添加角色', '新增角色', 800, '/system/roles/create', 'admin:role:add', 1, NOW(), NOW(), 1),
(803, '编辑角色', '编辑角色', 800, '/system/roles/edit', 'admin:role:update', 1, NOW(), NOW(), 1),
(804, '删除角色', '删除角色', 800, '/system/roles/delete', 'admin:role:delete', 1, NOW(), NOW(), 1),
(805, '角色授权', '配置角色可访问的权限菜单', 800, '/system/roles/permissions', 'admin:role:permission', 1, NOW(), NOW(), 1),
(806, '权限菜单', '查看权限菜单', 800, '/system/permissions', 'admin:permission:list', 1, NOW(), NOW(), 1),
(807, '添加权限菜单', '新增权限菜单', 800, '/system/permissions/create', 'admin:permission:add', 1, NOW(), NOW(), 1),
(808, '编辑权限菜单', '编辑权限菜单', 800, '/system/permissions/edit', 'admin:permission:update', 1, NOW(), NOW(), 1),
(809, '删除权限菜单', '删除权限菜单', 800, '/system/permissions/delete', 'admin:permission:delete', 1, NOW(), NOW(), 1),

(900, '审核统计', '审核日志、评论管理和统计', 0, '/audit-stat', 'admin:audit-stat', 1, NOW(), NOW(), 1),
(901, '审核日志', '查看课程/商品审核日志', 900, '/audit-stat/audit-logs', 'admin:audit:log', 1, NOW(), NOW(), 1),
(902, '评论管理', '查看全站评论', 900, '/audit-stat/comments', 'admin:comment:list', 1, NOW(), NOW(), 1),
(903, '删除评论', '删除或拦截评论', 900, '/audit-stat/comments/delete', 'admin:comment:delete', 1, NOW(), NOW(), 1),
(904, '运营统计', '查看搜索、购买、点赞、收藏等统计', 900, '/audit-stat/statistics', 'admin:stats:view', 1, NOW(), NOW(), 1)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
perm_desc = VALUES(perm_desc),
parent_id = VALUES(parent_id),
path = VALUES(path),
perms = VALUES(perms),
status = VALUES(status),
update_time = NOW(),
update_by = 1;

DELETE FROM qf_role_permission WHERE role_id IN (1, 2, 3, 4);

-- 教师：自己的课程、课程评论、微圈内容
INSERT INTO qf_role_permission (id, role_id, permission_id) VALUES
(100001, 1, 100),(100002, 1, 101),
(100003, 1, 200),(100004, 1, 201),(100005, 1, 202),(100006, 1, 203),(100007, 1, 204),(100008, 1, 205),(100009, 1, 207),(100010, 1, 208),(100011, 1, 209),(100012, 1, 210),(100013, 1, 211),(100014, 1, 212),
(100015, 1, 300),(100016, 1, 301),(100017, 1, 302),(100018, 1, 303),(100019, 1, 304),(100020, 1, 305),
(100021, 1, 900),(100022, 1, 902),(100023, 1, 903);

-- 运营：订单、课程/商品、优惠券、轮播图、评论、统计
INSERT INTO qf_role_permission (id, role_id, permission_id)
SELECT 200000 + id, 2, id FROM qf_permission
WHERE id IN (
100,101,
200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,
300,301,302,303,304,305,306,
400,401,402,403,404,405,406,407,408,409,410,
500,501,502,503,504,
600,601,602,603,604,605,606,607,608,609,610,611,612,613,
900,901,902,903,904
);

-- 人事：用户、员工、部门、申请审核
INSERT INTO qf_role_permission (id, role_id, permission_id)
SELECT 300000 + id, 3, id FROM qf_permission
WHERE id IN (
100,101,
700,701,702,703,704,705,706,707,709,710,711,712,713,714
);

-- 管理员：全部权限
INSERT INTO qf_role_permission (id, role_id, permission_id)
SELECT 400000 + id, 4, id FROM qf_permission;
