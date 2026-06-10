## SIYUN 项目 - 文件上传需求汇总清单

### 📋 项目信息
- **项目名**: SIYUN
- **日期**: 2026-06-08
- **统计**: 共 23 个实体类，7 个表涉及文件上传

---

## 📤 文件上传字段统计

### 一、核心上传字段汇总

| 序号 | 实体类 | 表名 | 字段名 | 字段类型 | 说明 | 备注 |
|------|--------|------|--------|---------|------|------|
| 1 | PositionApply | js_position_apply | fileUrl | String | 作品/资料路径 | ★必需上传 |
| 2 | Course | js_course | coverUrl | String | 课程封面图片 | ★必需上传 |
| 3 | Course | js_course | videoUrl | String | 课程视频源文件 | ★必需上传 |
| 4 | Course | js_course | detailDesc | String | 课程详情描述（富文本） | ⚠️ 可能含图片 |
| 5 | MomentsArticle | js_moments_article | coverUrl | String | 文章封面图片 | ★必需上传 |
| 6 | MomentsArticle | js_moments_article | content | String | 文章内容（富文本） | ⚠️ 可能含图片 |
| 7 | Goods | js_goods | mainPicUrl | String | 商品主图路径 | ★必需上传 |
| 8 | Coupon | js_coupon | imgUrl | String | 优惠券图片 | ★需要上传 |
| 9 | Carousel | js_carousel | picUrl | String | 轮播图片 | ★需要上传 |
| 10 | UserFeedback | js_user_feedback | picUrl | String | 反馈上传图片 | ★需要上传 |
| 11 | BackUser | js_back_user | avataUrl | String | 后台用户头像 | ★需要上传 |
| 12 | FrontUser | js_front_user | avataUrl | String | 前台用户头像 | ★需要上传 |

---

## 📁 按功能模块分类

### 1. **用户相关** (2个表)
```
BackUser (js_back_user)
├─ avataUrl: 后台用户头像 (JPG/PNG, 推荐 ≤2MB)

FrontUser (js_front_user)
├─ avataUrl: 前台用户头像 (JPG/PNG, 推荐 ≤2MB)
```

### 2. **课程管理** (1个表, 3个字段)
```
Course (js_course)
├─ coverUrl: 课程封面 (JPG/PNG, 推荐 ≤5MB, 分辨率1280x720)
├─ videoUrl: 视频源文件 (MP4/MKV/AVI等, 通常 ≤500MB)
└─ detailDesc: 富文本内容 (可能含图片)
```

### 3. **内容发布** (1个表, 2个字段)
```
MomentsArticle (js_moments_article)
├─ coverUrl: 文章封面 (JPG/PNG, 推荐 ≤5MB)
└─ content: 富文本内容 (可能含图片)
```

### 4. **商品管理** (1个表)
```
Goods (js_goods)
├─ mainPicUrl: 商品主图 (JPG/PNG, 推荐 ≤5MB, 分辨率800x600以上)
```

### 5. **职位申请** (1个表)
```
PositionApply (js_position_apply)
├─ fileUrl: 作品/资料 (PDF/DOC/ZIP等, 推荐 ≤20MB)
```

### 6. **营销管理** (2个表)
```
Coupon (js_coupon)
├─ imgUrl: 优惠券图片 (JPG/PNG, 推荐 ≤2MB)

Carousel (js_carousel)
├─ picUrl: 轮播图片 (JPG/PNG, 推荐 ≤5MB, 分辨率1920x620)
```

### 7. **用户反馈** (1个表)
```
UserFeedback (js_user_feedback)
├─ picUrl: 反馈图片 (JPG/PNG, 推荐 ≤5MB)
```

---

## ⚙️ 建议的 YAML 配置模板

```yaml
spring:
  servlet:
    multipart:
      # 单个文件最大大小
      max-file-size: 500MB
      # 请求最大大小
      max-request-size: 500MB
      # 内存中的临界值
      file-size-threshold: 5MB

# 自定义文件上传配置
upload:
  # 根路径
  root-path: /data/siyun-upload
  
  # 各类型文件配置
  profile:
    # 用户头像
    avatar:
      path: /avatars
      max-size: 2MB
      allowed-types: jpg,jpeg,png,gif
      
    # 课程相关
    course:
      cover:
        path: /courses/covers
        max-size: 5MB
        allowed-types: jpg,jpeg,png
      video:
        path: /courses/videos
        max-size: 500MB
        allowed-types: mp4,mkv,avi,flv
      detail:
        path: /courses/details
        max-size: 50MB
        allowed-types: jpg,jpeg,png,gif
        
    # 文章内容
    article:
      cover:
        path: /articles/covers
        max-size: 5MB
        allowed-types: jpg,jpeg,png
      content:
        path: /articles/contents
        max-size: 50MB
        allowed-types: jpg,jpeg,png,gif
        
    # 商品相关
    goods:
      image:
        path: /goods/images
        max-size: 5MB
        allowed-types: jpg,jpeg,png
        
    # 职位申请
    position:
      file:
        path: /position-apply/files
        max-size: 20MB
        allowed-types: pdf,doc,docx,xls,xlsx,zip,rar
        
    # 营销相关
    marketing:
      coupon:
        path: /marketing/coupons
        max-size: 2MB
        allowed-types: jpg,jpeg,png
      carousel:
        path: /marketing/carousels
        max-size: 5MB
        allowed-types: jpg,jpeg,png
        
    # 用户反馈
    feedback:
      image:
        path: /feedback/images
        max-size: 5MB
        allowed-types: jpg,jpeg,png,gif
```

---

## 📌 实现建议

### 1. **上传字段对应的实体类属性**

```java
/**
 * 在实体类中标注上传相关注解 (建议)
 */
@Data
public class Course {
    // ...existing fields...
    
    @UploadFile(type = "image", maxSize = "5MB")
    private String coverUrl;
    
    @UploadFile(type = "video", maxSize = "500MB")
    private String videoUrl;
}
```

### 2. **建议的文件存储策略**

- **头像文件**: `/upload/avatars/{userId}/{fileName}`
- **课程数据**: `/upload/courses/{courseId}/{type}/{fileName}`
- **文章数据**: `/upload/articles/{articleId}/{type}/{fileName}`
- **商品数据**: `/upload/goods/{goodsId}/{fileName}`
- **营销数据**: `/upload/marketing/{type}/{fileName}`
- **职位申请**: `/upload/positions/{applyId}/{fileName}`
- **用户反馈**: `/upload/feedback/{feedbackId}/{fileName}`

### 3. **文件上传安全建议**

- ✅ 校验文件类型（不仅仅是后缀名）
- ✅ 限制文件大小
- ✅ 生成新的文件名（避免覆盖）
- ✅ 病毒扫描（可选，生产环境推荐）
- ✅ 异步上传处理
- ✅ CDN 加速服务（可选）

---

## 📊 字段类型统计

| 文件类型 | 数量 | 实体类示例 |
|---------|------|----------|
| 图片 (JPG/PNG/GIF) | 10 | Course, MomentsArticle, Goods, BackUser, FrontUser, Coupon, Carousel, UserFeedback等 |
| 视频 (MP4等) | 1 | Course |
| 富文本（含图片） | 2 | Course.detailDesc, MomentsArticle.content |
| 其他文档 (PDF/DOC等) | 1 | PositionApply |

---

## 🔧 下一步操作

1. **根据实际需求修改 `application.yaml`**
   - 调整文件大小限制
   - 配置存储路径
   - 设置允许的文件类型

2. **创建文件上传服务层**
   - `FileUploadService` - 文件上传服务
   - `FileStorageStrategy` - 存储策略
   - `FileValidator` - 文件验证器

3. **实现上传接口**
   - 单文件上传
   - 批量文件上传
   - 文件删除
   - 文件下载

4. **前端实现**
   - 文件选择器组件
   - 进度条展示
   - 类型验证提示

---

## 📝 备注

- ⚠️ 标注"可能含图片"的富文本字段需要特殊处理
- ★ 标注"必需上传"的字段应该在项目初期优先实现
- 所有涉及用户头像的字段都需要考虑默认头像处理
- 大文件上传（视频）建议使用分片上传技术


