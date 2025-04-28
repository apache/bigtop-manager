# 开发环境搭建
## 前置条件
### 前端
* Vue: 3.4.x
* Vite: 5.x
* NodeJS: v18.x
* Pnpm: v8.x
* 组件库: Ant Design Vue 4.x

### 后端
* Git: 任意版本
* JDK: JDK17 或以上
* Database: Postgres(16+) 或 MySQL(8+)
* Maven: 推荐使用3.8以上版本
* 开发工具: Intellij IDEA

## 设置
### 获取代码
首先，您需要通过以下命令从 Github 中拉取 Bigtop Manager 源码:

`git clone git@github.com:apache/bigtop-manager.git`

### 编译
获取代码后，部分依赖需要先编译项目才能使用，否则会报错，请运行以下命令:

`./mvnw clean install -DskipTests`

### 初始化
首先您需要初始化您的数据库，数据库文件在 `bigtop-manager-server/src/main/resources/ddl/` 目录下，请使用对应的脚本来初始化您的数据库，目前仅支持 `Postgres` 以及 `MySQL`

并且在 `bigtop-manager-server/src/main/resources/application.yml` 文件中修改您的数据库信息

### 开发
在编译完项目后您就可以开始开发了

针对 Java 项目，直接使用 Intellij IDEA 的 `Debug` 能力即可

针对 Vue 项目，请运行如下命令:

```
cd bigtop-manager-ui
pnpm dev
```

然后使用浏览器访问 `localhost:5173` 即可，接下来就请尽情享受您的开发之旅吧！

### 开发模式
为降低大数据组件开发环境依赖复杂度，我们支持了开发模式：

#### 环境解耦设计
传统部署要求开发者搭建完整 Linux 服务集群，存在以下痛点：
* 环境配置复杂度高，提升了开发门槛
* 非组件问题（如调度问题等）易导致开发受阻

#### 轻量化调试机制
通过`DEV_MODE=true`环境变量激活开发模式，实现：
* **Mock 组件操作**：Agent 自动拦截组件调用，返回预设成功状态
* **跨平台支持**：完整兼容 Windows/MacOS/Linux 开发环境（推荐 IntelliJ IDEA）

#### 快速启用
用户通过以下方式即可启用：
![DEV_MODE](https://github.com/user-attachments/assets/d0e59fad-4287-4be5-a57c-d5c656e0dbb2)

# 模块及功能
| 模块                        | 介绍                                              | 
|---------------------------|-------------------------------------------------|
| **bigtop-manager-agent**  | 会被安装到每台主机上，对每台主机上的服务进行管理                        |
| **bigtop-manager-ai**     | 包含一些 AI 助手相关的代码                                 |
| **bigtop-manager-bom**    | 定义了项目中所有依赖及其版本                                  |
| **bigtop-manager-common** | 一些公共工具类                                         |
| **bigtop-manager-dao**    | 与数据库进行交互                                        |
| **bigtop-manager-dist**   | 打包后的内容会放在该模块下，包括 `Server` 和 `Agent` 的 tar 包     |
| **bigtop-manager-grpc**   | Server 应用与 Agent 应用通过 gRPC 交互，该模块包含所有 gRPC 服务定义 |
| **bigtop-manager-server** | 管理端的主要代码                                        |
| **bigtop-manager-stack**  | 包含各个组件栈中的组件及其操作脚本                               |
| **bigtop-manager-ui**     | 前端代码                                            |

# 贡献流程
## 代码规范
我们需要保证我们的代码格式符合要求

Java:
```
./mvnw clean spotless:apply
```

Vue:
```
cd bigtop-manager-ui
pnpm prettier
```

## 单元测试
在提交代码前，请确保所有单测均通过

Java:
```
./mvnw clean test -Dskip.pnpm -Dskip.installnodepnpm -Dskip.pnpm.test
```

Vue:
```
./mvnw -pl bigtop-manager-ui test
```

## 创建 Issue
1、首先进入 Apache Jira 中的 [Bigtop](https://issues.apache.org/jira/projects/BIGTOP) 项目

2、创建 Issue

3、确保 Issue 的 Summary 是英文描述，如果可以的话请将细节写到 Description 下，也需要使用英文

4、如果是给 Bigtop Manager 提交项目，点击 Components 选项，选中 bigtop-manager，并且 Fix Version 也需要选择对应的版本，Bigtop Manager 以 bm- 开头，如 bm-1.0.0 代表这个 issue 将在 Bigtop Manager 1.0.0 版本修复。Affects Version 可选，规则同上

## 提交代码
1、获取 Issue 编号，编号可从 Jira 页面或者 URL 中获取，如当前的 URL 为: [https://issues.apache.org/jira/browse/BIGTOP-4162](https://issues.apache.org/jira/browse/BIGTOP-4162)，则编号为 BIGTOP-4162

2、创建本地分支，建议一个 Issue 对应一个分支，如 `git checkout -b bigtop-4162`

3、提交代码至该分支中，并且将分支推到你的 Github 上的 Fork 的仓库中

4、创建 Pull Request，其中 Title 的命名规则为 `ISSUE编号: 描述`，如 `BIGTOP-4162: Add health check for components`，若 PR 较复杂，建议 Issue 和 PR 都编写 Description 来解释具体用途

5、确保 Github CI 均通过，若有一个失败则 PR 将不会被 Review

6、CI 正常后等待 Maintainer Review 你的 PR，若有 Comment 请及时处理，Review 通过后即可被合并