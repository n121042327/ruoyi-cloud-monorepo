# 后端 Docker 部署手册（本仓库口径）

本目录的 `docker-compose.yml` 来自 RuoYi-Cloud-Plus 上游，形态是「中间件 + 上游预编译的应用镜像」。
本仓库 fork 出来以后加了 `ruoyi-edu` 模块、改过 `ruoyi-system` 与 `ruoyi-resource`，
所以下面的步骤在本目录原始文件基础上补了三处：edu 服务节点、`ruoyi-edu.yml` 配置、本地镜像构建脚本。

## 0. 前置

- 启动 Docker Desktop，等到 `docker ps` 能正常返回（引擎没起来时 compose 会直接连接失败）。
- Windows + Docker Desktop 注意两点：
  1. 本 compose 所有服务都是 `network_mode: "host"`，需要在 Docker Desktop 设置里开启 host networking，否则起不来；
  2. 所有卷路径都是 Linux 绝对路径（`/docker/...`），会落在 WSL2 的 VM 文件系统里，不在 Windows 磁盘上。
- 端口占用：应用占用 8080（网关）、8091、8800/17888、9100、9201~9205、9210（auth）、**9211（edu）**、9401、9402。
  其中 edu 原本写的是 9210，与 `ruoyi-auth` 冲突，已改成 9211（同步改在 `application.yml` 与 edu 的 Dockerfile）。
- 构建剖面与 Nacos 地址：`pom.xml` 的 `dev` 剖面是 `activeByDefault`，把 `@nacos.server@` 解析成 `127.0.0.1:8848`、
  `@profiles.active@` 解析成 `dev`。因为所有容器都用 host 网络，容器内的 `127.0.0.1:8848` 就是宿主机的 Nacos；
  同时 **Nacos 的命名空间必须用 `dev`**（`namespace: ${spring.profiles.active}`），否则服务会注册到 `public` 而读不到配置。

## 1. 起中间件

```powershell
cd services\RuoYi-Cloud-Plus
docker compose -f script/docker/docker-compose.yml up -d mysql nacos redis minio
docker compose -f script/docker/docker-compose.yml ps
```

`mysql` 节点会建好空库 `ry-cloud`（root 密码 `ruoyi123`）；`nacos` 是上游定制镜像，端口 8848。

## 2. 建库导表

`script/sql/ry-cloud.sql` 只含 `sys_*` 等基础表，**不含任何 `edu_` 表**；教育域的表在
`docs/40-detailed-design/migrations/V1__…V6__`，必须在基础表之后按序号执行：

```powershell
# 基础表
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -pruoyi123 ry-cloud < script/sql/ry-cloud.sql

# 教育域表（V1 -> V6 顺序，V6 是学生联系电话的增量列）
Get-ChildItem ..\..\docs\40-detailed-design\migrations\*.sql | Sort-Object Name |
  ForEach-Object { docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -pruoyi123 ry-cloud < $_.FullName }
```

`script/sql/ry-config.sql`（Nacos 配置库）、`ry-job.sql`、`ry-workflow.sql`、`ry-seata.sql`
按需导入；用上游 `ruoyi/ruoyi-nacos:2.6.2` 镜像时，配置库已经自带。

> **含中文的 SQL 必须带 `--default-character-set=utf8mb4`**（所有库，包括 `ry-config.sql`）。
> 不带这个参数时客户端按 latin1 解释 UTF-8 文件，中文会被**双重编码**写进库：
> 表现就是 Nacos 控制台里命名空间的「开发环境 / 生产环境」变成 `å¼€å‘ç¯å¢ƒ` 这类乱码，
> 或者导 `ry-cloud.sql` 时报 `Data too long for column 'nick_name'`。
> 判断方法：`select hex(tenant_desc) from tenant_info` —— 正常应是 `E5BC80E58F91E78EAFE5A283`（“开发环境”），
> 乱码则是 `C3A5C2BC...`（把 UTF-8 字节当 latin1 再编码的结果）。

## 3. 导入 Nacos 配置

把 `script/config/nacos/` 下的 yml 逐个导入 Nacos（命名空间用 `dev`，组 `DEFAULT_GROUP`）：

| 文件 | 说明 |
|---|---|
| `application-common.yml` | 公共配置（多租户 `tenant.excludes`、MyBatis-Plus 等） |
| `datasource.yml` | 数据源四组；**密码已改为 `ruoyi123`**，与 compose 里的 `MYSQL_ROOT_PASSWORD` 对齐 |
| `ruoyi-edu.yml` | 本次新增；给教育域配主数据源 master，并带 `edu.data-scope.enabled` 开关与日志级别 |
| 其余 `ruoyi-*.yml` | 各服务的专属配置 |

edu 的 `application.yml` 里写的是 `optional:nacos:ruoyi-edu.yml`，缺了不会报错，但**没有 `ruoyi-edu.yml` 就没有数据源**，
所以这个文件是实际必需的。

### 批量同步（推荐，避免手工粘贴）

Nacos 里的配置**不会**跟着仓库自动更新；`script/config/nacos/*.yml` 改过之后必须重新导入。
除了在控制台逐个粘贴，也可以直接用 Nacos 的 Open API 批量 upsert（下面的脚本会把 13 个 yml 全部推到 `dev` 与 `prod`）：

```powershell
cd services\RuoYi-Cloud-Plus\script\config\nacos
$tok = (Invoke-RestMethod -Uri "http://127.0.0.1:8848/nacos/v1/auth/login" -Method Post -Body "username=nacos&password=nacos").accessToken
foreach ($ns in @("dev","prod")) {
  foreach ($f in Get-ChildItem -Filter *.yml) {
    $content = Get-Content $f.FullName -Raw -Encoding UTF8
    $form = "dataId=" + [uri]::EscapeDataString($f.Name) + "&group=DEFAULT_GROUP&tenant=$ns&type=yaml&content="
          + [uri]::EscapeDataString($content) + "&accessToken=" + [uri]::EscapeDataString($tok)
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($form)
    Invoke-RestMethod -Uri "http://127.0.0.1:8848/nacos/v1/cs/configs" -Method Post -Body $bytes `
      -ContentType 'application/x-www-form-urlencoded'
  }
}
```

> 注意：`ruoyi-config.sql`（Nacos 配置库的种子数据）里带的是**上游版本**的配置，
> 直接用它会缺 `ruoyi-edu.yml`、且 `datasource.yml` 的密码、`application-common.yml` 的 `tenant.excludes`、
> `ruoyi-gateway.yml` 的 edu 路由都会是旧的。**导入 `ry-config.sql` 之后必须再跑一次上面的同步。**
>
> 另外：这段 API 同步只提交 `content`，**不传 `desc` 时 Nacos 会把该配置的「描述」清空**
> （控制台配置列表的描述列变空）。描述只影响可读性，不影响服务读取；需要保留就把 `ry-config.sql` 里
> 各 dataId 的 `c_desc` 回填一下（`update config_info set c_desc = ... where data_id='...' and tenant_id='dev'`）。

## 4. 构建本地镜像

compose 里 `ruoyi/ruoyi-edu:2.6.2` 这个镜像上游不存在；`ruoyi/ruoyi-system:2.6.2`、
`ruoyi/ruoyi-resource:2.6.2` 虽然是上游镜像，但**不含本仓库新增的远程方法**
（`RemoteUserService.resetPassword` / `changeAccountStatus`、`RemoteFileService.downloadByUrl`），
直接跑会让 edu 的「重置密码」「学生照片」在运行期报方法不存在。因此这三个模块要用本仓库构建：

```powershell
powershell -ExecutionPolicy Bypass -File script\docker\build-edu-images.ps1
```

脚本做的事：`mvn -DskipTests package`（edu + system + resource 及其依赖）→
用各模块自己的 `Dockerfile` 打成本地镜像，tag 与 compose 里的 `image:` 完全一致
（本地同名镜像会覆盖上游 pull 下来的镜像，仅影响本机）。

## 5. 起应用

```powershell
# 最小可用集：网关 + 认证 + 系统 + 文件服务 + 教育域
docker compose -f script/docker/docker-compose.yml up -d gateway auth system resource edu

# 或者按需整栈
docker compose -f script/docker/docker-compose.yml up -d
```

首次启动顺序建议：`mysql/nacos/redis` 就绪 → 导入 SQL 与 Nacos 配置 → 再起应用；否则服务会在启动时因连不上 Nacos 而退出。

## 6. 验证

```powershell
docker compose -f script/docker/docker-compose.yml ps          # 全部 Up
# Nacos 控制台 http://localhost:8848/nacos 看服务列表里有没有 ruoyi-edu（端口 9211）
# 网关登录拿 token 后打教育域接口冒烟：
#   GET  http://localhost:8080/edu/grade/list
#   POST http://localhost:8080/edu/import/validate 等
```

## 已知限制

- `edu.data-scope.enabled` 默认 `false`：数据范围拦截器开启前要按 `schema.yaml` 的表清单核对范围列映射与插件链顺序（CR-093 / GAP-096）。
- 学生照片走 `ruoyi-resource` 的对象存储，需要该服务的 OSS 配置就绪（MinIO 默认账号见 compose）。
- 导入导出的结果文件下载仍是「文件引用 + 签名描述」，还没有改成真实流式下载（GAP-093 尾项，阶段 8 联调项）。

## 常见启动问题

### 1. 报 `I/O failure while processing configuration class` / 某个类 `cannot be opened because it does not exist`

**原因**：`target/classes` 里残留了**源码已删除**的 class（IDE 与 Maven 共用同一输出目录时最容易出现）。
打包时它被塞进 jar，运行时被 Spring 组件扫描到，而它依赖的接口/类又不在正常依赖里，于是类解析阶段直接报文件不存在。

典型实例（2026-10-08）：`ruoyi-gateway` 启动失败，报
`BeanDefinitionStoreException: I/O failure while processing configuration class [org.dromara.gateway.filter.ForwardHeadersFilter]`，
`Caused by: FileNotFoundException: ... server/mvc/filter/HttpHeadersFilter$RequestHttpHeadersFilter.class`。
源码里根本没有这个文件（git 历史也没有），但 `target/classes` 与 jar 里都有——是早前「MVC 网关」试验留下的产物。

**排查**：比对 `target/classes` 与 `src/main/java`，找出没有对应源码的 class（MapStruct 生成的 `*MapperImpl` /
`AutoMapperConfig__N` 属正常，要排除）。在 `services/RuoYi-Cloud-Plus` 下跑：

```powershell
python tools\check_stale_classes.py   # 仓库自带的诊断脚本，会列出没有源码的 class
```

也可以用一条命令快速确认某个类在 jar 里有没有：

```powershell
jar tf ruoyi-gateway\target\ruoyi-gateway.jar | findstr /i ForwardHeadersFilter
```

**修复**：`mvn -o -DskipTests -pl <模块> -am clean package`。
**删过类、改过包名之后一定要 `clean`**；在 IDE 里跑的话先 Build → Rebuild Project，或者直接换成 Maven 打出的最新 jar。
