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

Bigtop-Manager-ui is the front-end UI of the Manager platform, which stores code about the interaction between the platform and users, interface display, control styles, etc.

## Prerequisites

Vite: Version 4.4.5

Typescript: Version 5.0.2

Vue: Version 3.3.4

Editor: VsCode

## Project Structure

```
—————————————————— public           static resources
—————————————————— src              project source code
——————————— API                     calls the backend interface
——————————— assets                  static assets
——————————— components              customize components
——————————— layouts                 vue pages
——————————— locales                 internationalization
——————————— pages                   components
——————————— router                  router
———————————store                    global persistence
———————————types                    data type
——————————— utils                   utility function
——————————— App.vue                 project root component
———————————main.ts                  Project packaging portal
——————————————————index.html        Project page
——————————————————package.json      Package management profile for your project
——————————————————vite.config.ts    Project configuration file
```
