-- 审核模块：停用无实际业务价值的运营统计，并补充创作者审核、问题反馈和在线客服权限。

UPDATE qf_permission
SET status = 0,
    update_time = NOW(),
    update_by = 1
WHERE id = 904;

UPDATE qf_permission
SET name = '创作者审核',
    perm_desc = '查看前台用户提交的创作者认证申请',
    parent_id = 900,
    path = '/audit-stat/creator-audit',
    update_time = NOW(),
    update_by = 1
WHERE id = 713;

UPDATE qf_permission
SET name = '审核创作者申请',
    perm_desc = '通过或驳回创作者认证申请',
    parent_id = 713,
    path = '/audit-stat/creator-audit/audit',
    update_time = NOW(),
    update_by = 1
WHERE id = 714;

INSERT INTO qf_permission
    (id, name, perm_desc, parent_id, path, perms, status, create_time, update_time, update_by)
VALUES
    (905, '问题反馈', '查看用户提交的问题反馈', 900, '/audit-stat/feedback', 'admin:feedback:list', 1, NOW(), NOW(), 1),
    (906, '处理问题反馈', '更新反馈处理状态并回复用户', 905, '/audit-stat/feedback/reply', 'admin:feedback:reply', 1, NOW(), NOW(), 1),
    (907, '在线客服', '查看用户客服会话与消息', 900, '/audit-stat/service', 'admin:service:list', 1, NOW(), NOW(), 1),
    (908, '回复客服消息', '在客服会话中回复用户', 907, '/audit-stat/service/reply', 'admin:service:reply', 1, NOW(), NOW(), 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    perm_desc = VALUES(perm_desc),
    parent_id = VALUES(parent_id),
    path = VALUES(path),
    perms = VALUES(perms),
    status = VALUES(status),
    update_time = NOW(),
    update_by = VALUES(update_by);

-- 运营人员可处理审核与客服；管理员保留完整权限。
INSERT INTO qf_role_permission (id, role_id, permission_id)
VALUES
    (500001, 2, 713),
    (500002, 2, 714),
    (500003, 2, 905),
    (500004, 2, 906),
    (500005, 2, 907),
    (500006, 2, 908),
    (500007, 4, 905),
    (500008, 4, 906),
    (500009, 4, 907),
    (500010, 4, 908)
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id),
    permission_id = VALUES(permission_id);
