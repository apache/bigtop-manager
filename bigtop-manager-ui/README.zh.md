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

# Bigtop-Manager-ui

Bigtop-Manager-ui是Manager平台的前端UI，存放有关平台与用户交互，界面展示，控件样式等代码。

## 先决条件

Vite：版本4.4.5

Typescript：版本5.0.2

Vue：版本3.3.4

编辑器：VsCode

## 项目结构

```
——————————————————public                公共静态资源
——————————————————src                   项目源代码
——————————— api                         调用后端接口
——————————— assets                      静态资源
——————————— components                  自定义组件
——————————— composables                 组合式函数
——————————— directives                  自定义指令
——————————— layouts                     整体页面布局
——————————— locales                     国际化
——————————— pages                       组件
——————————— plugins                     全局注册配置
——————————— router                      路由
——————————— store                       全局持久化
——————————— styles                      样式文件
——————————— types                       数据类型
——————————— utils                       工具函数
——————————— App.vue                     项目根组件
——————————— main.ts                     项目打包入口
—————————————————— index.html           项目页面
—————————————————— package.json         项目的包管理配置文件
—————————————————— vite.config.ts       项目配置文件
—————————————————— postcss.config.ts    postcss 配置文件
```
