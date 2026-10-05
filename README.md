# Reggie Take Out 外卖管理系统

基于 Spring Boot、MyBatis Plus 和 MySQL 的外卖管理项目，包含商家管理端和用户点餐端。管理端负责员工、分类、菜品、套餐和订单管理；用户端支持浏览商品、购物车、地址管理、下单及查看订单。

项目采用 Controller → Service → Mapper 分层结构。前端使用 Vue 页面和 Axios 请求接口，静态资源与后端一起运行，无需单独启动前端服务。

## 技术栈

| 技术 | 版本或用途 |
| --- | --- |
| Java | JDK 8 |
| Spring Boot | 2.4.5 |
| MyBatis Plus | 3.4.2，数据访问、分页和主键生成 |
| MySQL | 当前本地环境为 5.7.43 |
| Druid | 1.1.23，数据库连接池 |
| Lombok | 1.18.20 |
| Maven | 依赖管理、编译和打包 |
| Vue / Element UI | 管理端页面 |
| Vue / Vant | 用户端页面 |
| Axios | HTTP 请求 |

## 已实现功能

### 商家管理端

| 模块 | 功能 |
| --- | --- |
| 员工管理 | 登录、退出、新增、修改、分页查询、启用和禁用 |
| 分类管理 | 新增、修改、删除、分页查询，区分菜品分类和套餐分类 |
| 菜品管理 | 新增、修改、口味配置、分页查询、启售、停售、单个及批量删除 |
| 套餐管理 | 新增、修改、关联菜品及份数、分页查询、启售、停售、单个及批量删除 |
| 订单管理 | 分页查询、订单号和时间范围筛选、查看订单及商品明细、派送、完成 |
| 图片管理 | 上传图片、按文件名读取图片 |

### 用户点餐端

| 模块 | 功能 |
| --- | --- |
| 用户登录 | 手机验证码登录，首次登录自动创建用户，退出登录 |
| 商品浏览 | 按分类查询启售菜品和套餐，选择菜品口味 |
| 购物车 | 添加、减少商品、查询数量及金额、清空，按用户保存数据 |
| 地址管理 | 新增、修改、删除、查询、设置默认地址、下单时选择地址 |
| 下单 | 保存订单和商品明细、计算总金额、清空当前用户购物车 |
| 个人中心 | 最新订单、商品名称与数量、订单状态及实付金额 |
| 历史订单 | 分页加载订单及商品明细，已完成订单支持“再来一单” |

“再来一单”会按商品当前售价加入购物车，与已有同商品、同口味记录合并数量；商品已停售或删除时返回提示。

## 项目目录

```text
reggie_take_out/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/it/reggie/
    │   ├── ReggieApplication.java   # 启动类
    │   ├── common/                 # 统一响应、异常处理、上下文和 JSON 转换
    │   ├── config/                 # MVC、静态资源映射、MyBatis Plus 分页
    │   ├── controller/             # 接口入口
    │   ├── dto/                    # 菜品、套餐、订单及关联数据
    │   ├── entity/                 # 数据库实体
    │   ├── filter/                 # 登录检查
    │   ├── mapper/                 # 数据访问
    │   ├── service/                # 业务接口
    │   │   └── impl/               # 业务实现
    │   └── utils/                  # 验证码等工具
    └── resources/
        ├── application.yml         # 端口、数据库和图片目录配置
        ├── backend/                # 商家管理端
        │   ├── api/                # 接口调用
        │   ├── page/               # 功能页面
        │   ├── plugins/            # 前端依赖
        │   └── styles/
        └── front/                  # 用户点餐端
            ├── api/
            ├── page/
            ├── js/
            └── styles/
```

## 本地运行

### 1. 准备环境和数据库

安装 JDK 8、Maven 和 MySQL，准备名为 `reggie` 的数据库及数据表。

**当前仓库没有数据库初始化 SQL，也没有自动建表脚本。** 在新环境运行时，需要先导入项目对应的数据库备份或建表 SQL。只创建空数据库无法正常使用业务功能。

项目使用以下 11 张表：

| 表名 | 用途 |
| --- | --- |
| `employee` | 员工账号 |
| `category` | 菜品和套餐分类 |
| `dish` | 菜品 |
| `dish_flavor` | 菜品口味 |
| `setmeal` | 套餐 |
| `setmeal_dish` | 套餐与菜品关联 |
| `user` | 用户 |
| `address_book` | 用户地址 |
| `shopping_cart` | 购物车 |
| `orders` | 订单 |
| `order_detail` | 订单商品明细 |

### 2. 修改配置

配置文件：`src/main/resources/application.yml`。

按实际环境修改数据库连接、账号、密码和图片目录，例如：

```yaml
server:
  port: 8080

spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      username: your_mysql_user
      password: your_mysql_password

reggie:
  path: 'D:/img/'
```

保留配置文件中的其他配置项。项目当前图片目录为 `D:\img\`，上传时会尝试创建不存在的目录。运行进程需要拥有该目录的读写权限。

数据库中的图片字段保存文件名，实际图片保存在 `reggie.path` 指定的目录。迁移数据库时也需要迁移对应图片文件。

### 3. 启动服务

**IDEA 启动：**

1. 以 Maven 项目打开根目录，加载依赖。
2. 设置项目 JDK 为 Java 8。
3. 运行 `com.it.reggie.ReggieApplication` 的 `main` 方法。

**命令行启动：** 在项目根目录执行：

```bash
mvn spring-boot:run
```

**打包后运行：**

```bash
mvn clean package
java -jar target/reggie_take_out-1.0-SNAPSHOT.jar
```

首次加载依赖需要可用的 Maven 仓库连接。启动前确认 MySQL 已运行，且 `8080` 端口未被其他程序占用。

### 4. 访问页面

| 页面 | 地址 |
| --- | --- |
| 管理端登录 | http://localhost:8080/backend/page/login/login.html |
| 管理端首页 | http://localhost:8080/backend/index.html |
| 用户端登录 | http://localhost:8080/front/page/login.html |
| 用户端首页 | http://localhost:8080/front/index.html |
| 用户个人中心 | http://localhost:8080/front/page/user.html |

管理端使用 `employee` 表中的有效账号登录。登录页面预填了 `admin / 123456`，能否登录取决于数据库中的实际账号；新增员工的初始密码为 `123456`。

用户端登录步骤：

1. 输入手机号，点击“获取验证码”。
2. 在应用控制台找到 `code=xxxx` 日志，获取四位验证码。
3. 在同一浏览器会话中输入验证码并登录。

用户端按移动页面设计，建议使用浏览器的移动设备模式查看，例如设置为 375px 宽。

## 主要接口

接口统一返回 `R<T>`。`code = 1` 表示成功，`code = 0` 表示业务失败；未登录时返回 `msg = "NOTLOGIN"`，前端跳转到登录页。

```json
{
  "code": 1,
  "msg": null,
  "data": {},
  "map": {}
}
```

分页数据包含 `records`、`total`、`current`、`size`、`pages` 等字段。登录状态通过 Session 和 `JSESSIONID` Cookie 保存。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/employee/login`、`/employee/logout` | 员工登录、退出 |
| GET | `/employee/page` | 员工分页 |
| GET | `/category/page`、`/category/list` | 分类分页、列表 |
| GET | `/dish/page`、`/dish/list` | 菜品分页、列表 |
| POST | `/dish/status/{status}` | 修改菜品启售状态，使用 `ids` 参数 |
| GET | `/setmeal/page`、`/setmeal/list` | 套餐分页、列表 |
| POST | `/setmeal/status/{status}` | 修改套餐启售状态，使用 `ids` 参数 |
| POST | `/common/upload` | 上传图片，表单字段为 `file` |
| GET | `/common/download?name=文件名` | 读取图片 |
| POST | `/user/sendMsg`、`/user/login`、`/user/loginout` | 获取验证码、用户登录、退出 |
| GET | `/addressBook/list`、`/addressBook/{id}` | 查询当前用户地址 |
| POST / PUT / DELETE | `/addressBook` | 新增、修改、删除地址，删除使用 `ids` 参数 |
| GET / PUT | `/addressBook/default` | 查询、设置默认地址 |
| GET | `/shoppingCart/list` | 查询当前用户购物车 |
| POST | `/shoppingCart/add`、`/shoppingCart/sub` | 添加、减少商品 |
| DELETE | `/shoppingCart/clean` | 清空当前用户购物车 |
| POST | `/order/submit` | 下单，提交 `addressBookId`、`payMethod`、`remark` |
| GET | `/order/userPage` | 当前用户订单分页，包含 `orderDetails` |
| GET | `/order/page` | 管理端订单分页，支持 `number`、`beginTime`、`endTime` |
| GET | `/orderDetail/{id}` | 根据订单 ID 查询商品明细 |
| PUT | `/order` | 管理端更新订单状态，提交 `id` 和 `status` |
| POST | `/order/again` | 已完成订单再次加入购物车，提交订单 `id` |

分页接口使用 `page`、`pageSize` 参数。订单时间筛选格式为 `yyyy-MM-dd HH:mm:ss`。

## 业务约定

- 菜品、套餐启售状态：`0` 停售，`1` 启售。
- 分类类型：`1` 菜品分类，`2` 套餐分类。
- 启售中的菜品、套餐需要先停售才能删除；已关联套餐的菜品不能直接删除。
- 分类下仍有关联菜品或套餐时，不能删除该分类。
- 购物车商品数量减到 `0` 时删除该条记录。
- 下单会在同一事务中保存订单、保存明细并清空购物车。
- 菜品和套餐的 `price` 按“分”保存；购物车、订单及订单明细的 `amount` 按“元”保存。
- JSON 中的 `Long` 类型 ID 序列化为字符串，前端应保留字符串形式，避免大整数精度丢失。

订单状态：

| 值 | 含义 |
| --- | --- |
| `1` | 待付款 |
| `2` | 待派送 |
| `3` | 派送中 |
| `4` | 已完成 |
| `5` | 已取消 |

当前下单成功后进入状态 `2`；管理端支持 `2 → 3 → 4`，对应“派送”和“完成”。

## 当前实现范围

- 短信服务调用处于注释状态，验证码生成后写入 Session 并输出到控制台。
- 支付页面模拟下单成功流程，尚未对接微信、支付宝等真实支付服务。
- 个人中心的昵称、头像仍使用页面中的静态内容。
- 用户端套餐详情调用的 `/setmeal/dish/{id}` 接口尚未实现。
- 仓库中保留了 `front/cartData.json`，当前购物车查询已使用 `/shoppingCart/list` 的数据库数据。

## 常见问题

### 应用启动失败

查看完整启动日志中最后的 `Caused by`，核对数据库是否可连接、端口是否被占用，以及 Java 代码是否编译成功。新增 Mapper 需要注册为 MyBatis Mapper，例如添加 `@Mapper`。

### 页面提示系统接口 404

检查前端请求路径、HTTP 方法和 Controller 映射是否一致。后端新增接口后需要重新编译并重启应用。

### 页面仍使用旧代码

修改后端代码后重启项目；修改前端资源后按 `Ctrl + F5` 刷新。通过管理端首页进入功能页时，也要检查首页加载的页面地址是否为最新版本。

### 图片读取出现 FileNotFoundException

核对 `reggie.path`、数据库中的图片文件名和目录中的实际文件。数据库记录中有文件名，并不代表本地目录中存在对应图片。

### 用户端没有显示商品

确认已登录，分类下存在启售商品，并检查浏览器网络面板中 `/category/list`、`/dish/list`、`/setmeal/list` 的响应。购物车内容通过 `/shoppingCart/list` 单独查询。

### 个人中心没有最新订单

只有当前用户成功下单后才会显示最新订单。没有订单时，最新订单区域隐藏，历史订单页面显示“暂无订单”。
