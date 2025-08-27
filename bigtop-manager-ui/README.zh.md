<!---
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--->

# Bigtop Manager UI 

Bigtop Manager UI 是 Manager 平台的前端 UI，存放有关平台与用户交互，界面展示，控件样式等代码。

## 先决条件

- Node.js：`v20.9.0`

- pnpm: `v10.12.3`

- Vite：`v5.4.19`

- Typescript：`v5.9.2`

- Vue：`v3.4.37`

- 推荐编辑器：[Visual Studio Code](https://code.visualstudio.com/)

## 项目结构

```plaintext
├── public/                    # 公共静态资源
├── src/                       # 项目源代码目录
│   ├── api/                   # 后端接口封装
│   ├── assets/                # 静态资源
│   ├── components/            # 可复用组件目录
│   │   ├── base/              # 基础组件
│   │   ├── common/            # 常用复合组件
│   ├── composables/           # Vue 组合式函数
│   ├── directives/            # 自定义指令
│   ├── features/              # 业务组件
│   ├── layouts/               # 页面布局组件
│   ├── locales/               # 国际化资源（i18n）
│   ├── pages/                 # 页面级组件
│   ├── plugins/               # 插件与全局注册逻辑
│   ├── router/                # 路由配置
│   ├── store/                 # 全局状态管理
│   ├── styles/                # 全局样式
│   ├── types/                 # 全局类型声明（.d.ts）
│   ├── utils/                 # 工具函数
│   ├── App.vue                # 根组件
│   └── main.ts                # 项目入口文件
├── tests/                     # 单元测试或集成测试用例
├── index.html                 # HTML 模板文件
├── package.json               # 包管理与脚本配置文件
├── tsconfig.json              # TypeScript 配置文件
├── vite.config.ts             # Vite 配置文件
├── postcss.config.ts          # PostCSS 配置
└── vitest.config.ts           # Vitest 测试配置文件
```
