# API 契约摘要

管理端使用服务端 Session。登录前先获取 CSRF Token：

```text
GET /api/auth/csrf
Cookie: XSRF-TOKEN=...
```

所有 `POST`、`PUT`、`PATCH`、`DELETE` 请求携带：

```text
X-XSRF-TOKEN: <token>
Cookie: JSESSIONID=...
```

登录请求：

```json
{
  "username": "admin",
  "password": "admin123123"
}
```

后台概览：

```text
GET /api/admin/dashboard
```

返回轮播图、期刊、投稿数量及最近投稿记录。

管理员治理：

```text
PUT /api/admin/account/password
GET /api/admin/audit-logs?limit=100
```

投稿记录支持服务端筛选和分页：

```text
GET /api/admin/submissions?status=待处理&keyword=AI&page=1&size=20
GET /api/admin/submissions/export?status=待处理&keyword=AI
GET /api/admin/audit-logs?action=PUT&keyword=/journals&page=1&size=20
```

公开期刊详情：

```text
GET /api/public/journals/{id}
```

官网内容：

```text
GET /api/public/services
GET /api/public/experts
GET/POST/PUT/DELETE /api/admin/services
GET/POST/PUT/DELETE /api/admin/experts
```

轮播图：

```json
{
  "id": null,
  "title": "新的轮播图",
  "imageUrl": "/images/hero-center.jpg",
  "linkUrl": "/SCI",
  "sortOrder": 3,
  "enabled": true
}
```

期刊：

```json
{
  "id": null,
  "type": "SCI",
  "title": "Journal Name",
  "field": "计算机与人工智能",
  "indexType": "SCI",
  "cycle": "3-6个月",
  "description": "期刊简介",
  "documentName": "期刊介绍.pdf",
  "documentUrl": "/uploads/documents/uuid.pdf",
  "published": true
}
```

文件上传：

```text
POST /api/admin/uploads/image
Content-Type: multipart/form-data
字段名：file
支持：JPG、PNG、WebP、GIF，最大 10MB

POST /api/admin/uploads/document
Content-Type: multipart/form-data
字段名：file
支持：PDF、Word、Excel、PPT、TXT，最大 20MB
```

投稿：

```json
{
  "authorName": "作者姓名",
  "email": "author@example.com",
  "paperTitle": "论文标题",
  "targetType": "SCI",
  "message": "补充说明"
}
```
