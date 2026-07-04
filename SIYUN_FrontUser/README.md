# 思云课堂前端用户端

这是前端用户 App 端，使用 Vue3 + uniapp。后台管理端不放在这个项目里。

## 目录

```text
src/
  api/           后端接口封装
  common/        全局样式
  components/    通用组件
  pages/         uniapp 页面
  stores/        前端用户状态
  utils/         请求、格式化、本地兜底数据
```

## 已接入接口

- `/user/login`、`/user/register`、`/user/logout`、`/user/changePassword`
- `/siyun/course`、`/siyun/order`、`/siyun/coupons`
- `/siyun/startplay`、`/siyun/stopplay`
- `/siyun/like`、`/siyun/collect`、`/siyun/share`
- `/siyun/comment`、`/siyun/commentLike`、`/siyun/subComment`
- `/siyun/moments`、`/siyun/myMoments`、`/siyun/moment`

## 已知后端对接点

- `changePassword` 当前后端读取 session key 是 `user`，登录写入的是 `student`，需要统一后才能成功改密码。
- 多个 `GET` 接口使用了 `@RequestBody`，小程序/App 端通常会把 `data` 放到 query，后端建议改成 `@RequestParam` 或改为 `POST`。
- `/siyun/moment` 详情方法声明了 `@PathVariable Integer id`，但 `@GetMapping` 没有带 `/{id}`。

## Project Setup

```sh
npm install
```

### H5 开发

```sh
npm run dev:h5
```

H5 开发时默认代理：

```text
/api/user/login -> http://localhost:8081/user/login
```

App/小程序端默认请求：

```text
http://localhost:8081
```

真机调试时 `localhost` 指手机本机，需要通过环境变量换成电脑局域网 IP：

```sh
VITE_API_BASE_URL=http://你的电脑IP:8081 npm run dev:h5
```

### H5 构建

```sh
npm run build:h5
```

### App 构建

```sh
npm run build:app
```
