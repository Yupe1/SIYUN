# SIYUN 文件上传快速参考表

## 📋 完整需求清单（一页纸版本）

### 表格 1: 文件上传字段映射

| # | 实体类 | 字段名 | 数据库字段 | 文件类型 | 推荐大小 | 用途 |
|---|--------|--------|-----------|---------|--------|------|
| 1 | **BackUser** | avataUrl | avata_url | 图片 | ≤2MB | 后台用户头像 |
| 2 | **FrontUser** | avataUrl | avata_url | 图片 | ≤2MB | 前台用户头像 |
| 3 | **Course** | coverUrl | cover_url | 图片(1280x720) | ≤5MB | 课程封面 |
| 4 | **Course** | videoUrl | video_url | 视频 | ≤500MB | 课程视频 |
| 5 | **Course** | detailDesc | detail_desc | 富文本+图片 | ≤50MB | 课程详情 |
| 6 | **MomentsArticle** | coverUrl | cover_url | 图片 | ≤5MB | 文章封面 |
| 7 | **MomentsArticle** | content | content | 富文本+图片 | ≤50MB | 文章内容 |
| 8 | **Goods** | mainPicUrl | main_pic_url | 图片(800x600) | ≤5MB | 商品主图 |
| 9 | **PositionApply** | fileUrl | file_url | 文档 | ≤20MB | 职位申请资料 |
| 10 | **Coupon** | imgUrl | img_url | 图片 | ≤2MB | 优惠券图片 |
| 11 | **Carousel** | picUrl | pic_url | 图片(1920x620) | ≤5MB | 轮播图 |
| 12 | **UserFeedback** | picUrl | pic_url | 图片 | ≤5MB | 反馈图片 |

---

## 🗂️ 按模块的存储路径规划

### 用户模块
```
/avatars/
├── /users/{frontUserId}/  ← 前台用户头像
├── /backstaffs/{backUserId}/  ← 后台人员头像
```

### 课程模块
```
/courses/
├── /covers/{courseId}/  ← 课程封面
├── /videos/{courseId}/  ← 视频文件
├── /details/{courseId}/  ← 详情图片
```

### 内容模块（微圈）
```
/articles/
├── /covers/{articleId}/  ← 文章封面
├── /contents/{articleId}/  ← 编辑器图片
```

### 商品模块
```
/goods/
├── /images/{goodsId}/  ← 主图
├── /detail-images/{goodsId}/  ← 详情图片
```

### 职位招聘
```
/positions/
├── /applies/{positionApplyId}/  ← 申请资料
```

### 营销模块
```
/marketing/
├── /coupons/{couponId}/  ← 优惠券图
├── /carousels/{carouselId}/  ← 轮播图
├── /banners/{bannerId}/  ← 活动横幅
```

### 用户反馈
```
/feedback/
├── /images/{feedbackId}/  ← 反馈图片
```

---

## ⚙️ YAML 配置快速清单

### 核心参数（必填）
```yaml
upload:
  root-path: /data/siyun-upload  # 存储根路径
  enable-local-storage: true      # 是否本地存储
  url-prefix: /uploads/           # 访问前缀

spring:
  servlet:
    multipart:
      max-file-size: 500MB        # 单文件最大值
      max-request-size: 500MB     # 请求最大值
```

### 按文件类型的配置
```
avatar:
  allowed-types: jpg,jpeg,png,gif,webp
  max-size: 2MB
  enable-compress: true

course-cover:
  allowed-types: jpg,jpeg,png,webp
  width: 1280
  height: 720
  max-size: 5MB

course-video:
  allowed-types: mp4,mkv,avi,flv,mov
  max-size: 500MB

image:
  allowed-types: jpg,jpeg,png,gif,webp
  max-size: 5MB

document:
  allowed-types: pdf,doc,docx,xls,xlsx,zip
  max-size: 20MB
```

---

## 📊 文件类型支持速查表

| 文件类型 | 支持格式 | 例外字段 | 建议处理 |
|---------|--------|--------|--------|
| **图片** | JPG, PNG, GIF, WEBP | 所有图片字段 | 压缩 + CDN |
| **视频** | MP4, MKV, AVI, FLV, MOV | Course.videoUrl | 转码 + 缩略图 |
| **富文本** | JPG, PNG, GIF | Course.detailDesc, MomentsArticle.content | 图片处理 |
| **文档** | PDF, DOC, DOCX, XLS, XLSX, ZIP | PositionApply.fileUrl | 病毒扫描 |

---

## 🚀 实现优先级

### Phase 1: 基础（必须）
- [ ] 用户头像上传 (BackUser, FrontUser)
- [ ] 课程封面上传 (Course.coverUrl)
- [ ] 商品主图上传 (Goods.mainPicUrl)

### Phase 2: 核心功能（重要）
- [ ] 课程视频上传 (Course.videoUrl)
- [ ] 文章内容上传 (MomentsArticle)
- [ ] 职位申请资料上传 (PositionApply.fileUrl)

### Phase 3: 功能完善（可选）
- [ ] 营销图片上传 (Coupon, Carousel)
- [ ] 用户反馈图片 (UserFeedback.picUrl)
- [ ] 富文本图片处理 (Course.detailDesc)

---

## 🔍 字段与YAML配置对应关系

```
BackUser.avataUrl          ← upload.profile.avatar
FrontUser.avataUrl         ← upload.profile.avatar
Course.coverUrl            ← upload.profile.course.cover
Course.videoUrl            ← upload.profile.course.video
Course.detailDesc          ← upload.profile.course.detail
MomentsArticle.coverUrl    ← upload.profile.article.cover
MomentsArticle.content     ← upload.profile.article.content
Goods.mainPicUrl           ← upload.profile.goods.image
PositionApply.fileUrl      ← upload.profile.position.file
Coupon.imgUrl              ← upload.profile.marketing.coupon
Carousel.picUrl            ← upload.profile.marketing.carousel
UserFeedback.picUrl        ← upload.profile.feedback.image
```

---

## 💾 推荐的文件命名规则

```
{timestamp}_{userId}_{random}.{ext}
示例: 1686480000_user123_a7f9s.jpg

或

{entityType}_{entityId}_{sequence}.{ext}
示例: course_456_cover.jpg
```

---

## ✅ 配置检查清单

- [ ] `root-path` 已设置为有效目录
- [ ] `max-file-size` 根据业务调整
- [ ] 所有文件类型都对应了 `allowed-types`
- [ ] 图片字段都配置了宽高限制
- [ ] 大文件配置了压缩或转码选项
- [ ] 敏感文件类型都配置了禁止列表
- [ ] CDN 或反向代理配置了 URL 前缀
- [ ] 存储路径有足够的磁盘空间
- [ ] 定期清理过期的临时文件

---

## 🎯 拷贝粘贴配置模板

```yaml
# 复制到 application.yaml

upload:
  root-path: /data/siyun-upload
  enable-local-storage: true
  url-prefix: /uploads/
  profile:
    avatar:
      path: /avatars
      max-size: 2MB
      allowed-types: jpg,jpeg,png,gif,webp
      enable-compress: true
      compress-quality: 85
    course:
      cover:
        path: /courses/covers
        max-size: 5MB
        allowed-types: jpg,jpeg,png,webp
        width: 1280
        height: 720
      video:
        path: /courses/videos
        max-size: 500MB
        allowed-types: mp4,mkv,avi,flv
    article:
      cover:
        path: /articles/covers
        max-size: 5MB
        allowed-types: jpg,jpeg,png,webp
    goods:
      image:
        path: /goods/images
        max-size: 5MB
        allowed-types: jpg,jpeg,png,webp
    position:
      file:
        path: /position-apply/files
        max-size: 20MB
        allowed-types: pdf,doc,docx,xls,xlsx,zip
    marketing:
      coupon:
        path: /marketing/coupons
        max-size: 2MB
        allowed-types: jpg,jpeg,png,webp
      carousel:
        path: /marketing/carousels
        max-size: 5MB
        allowed-types: jpg,jpeg,png,webp
    feedback:
      image:
        path: /feedback/images
        max-size: 5MB
        allowed-types: jpg,jpeg,png,gif,webp

spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
      file-size-threshold: 5MB
```

---

**最后更新**: 2026-06-08
**维护者**: 开发团队

