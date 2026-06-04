# SSL 证书目录

此目录用于存放 HTTPS/SSL 证书文件。

## 文件说明

| 文件名 | 说明 |
|--------|------|
| `fullchain.pem` | 完整证书链（包含服务器证书和中间证书） |
| `privkey.pem` | 私钥文件 |

## 获取证书

### 方式一：Let's Encrypt 免费证书（推荐）

1. 安装 Certbot：
```bash
sudo apt update
sudo apt install certbot
```

2. 获取证书（需要先停止占用 80 端口的服务）：
```bash
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com
```

3. 复制证书到此目录：
```bash
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ./ssl/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ./ssl/
sudo chown $USER:$USER ./ssl/*.pem
```

4. 设置自动续期：
```bash
sudo crontab -e
# 添加以下行（每天凌晨 2 点检查续期）
0 2 * * * certbot renew --quiet && docker-compose restart nginx
```

### 方式二：自签名证书（仅开发环境）

```bash
# 生成自签名证书（有效期 365 天）
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/privkey.pem \
  -out ssl/fullchain.pem \
  -subj "/C=CN/ST=State/L=City/O=Organization/CN=localhost"
```

### 方式三：购买商业证书

从证书颁发机构（CA）购买证书后，将以下文件放入此目录：
- `fullchain.pem` - 证书链文件
- `privkey.pem` - 私钥文件

## 启用 HTTPS

1. 确保证书文件已放置在此目录
2. 编辑 `nginx.conf`，取消注释 HTTPS 相关配置
3. 更新 `.env` 文件中的 `DOMAIN_NAME`
4. 重启服务：
```bash
docker-compose down
docker-compose up -d --build
```

## 安全提示

⚠️ **重要**：
- 私钥文件（`privkey.pem`）是敏感文件，切勿泄露
- 此目录已添加到 `.gitignore`，证书文件不会被提交到版本控制
- 生产环境建议设置文件权限：`chmod 600 ssl/*.pem`
