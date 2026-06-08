-- 1. 添加默认系统最高权限管理员账号
INSERT INTO user (username, password, nickname, role, status, created_at) 
SELECT 'admin', 'admin123abc', '系统管理员', 'ADMIN', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');
