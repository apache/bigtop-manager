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

Bigtop Manager UI is the front-end UI of the Manager platform, which stores code about the interaction between the platform and users, interface display, control styles, etc.

## Prerequisites

- Node.js：`v20.9.0`

- pnpm: `v10.12.3`

- Vite：`v5.4.19`

- Typescript：`v5.9.2`

- Vue：`v3.4.37`

- Editor Recommendation: [Visual Studio Code](https://code.visualstudio.com/)

## Project Structure

```plaintext
├── public/                    # Static assets
├── src/                       # Source code root
│   ├── api/                   # API request functions
│   ├── assets/                # Static assets 
│   ├── components/            # Reusable component directory
│   │   ├── base/              # Basic UI components
│   │   ├── common/            # Common composite components
│   ├── composables/           # Vue composables 
│   ├── directives/            # Custom directives
│   ├── features/              # Feature-based components
│   ├── layouts/               # Layout components
│   ├── locales/               # locale files (i18n)
│   ├── pages/                 # Page components
│   ├── plugins/               # Plugins and global registrations
│   ├── router/                # Routing configuration
│   ├── store/                 # Global state management
│   ├── styles/                # Global styles
│   ├── types/                 # Global TypeScript declarations (.d.ts)
│   ├── utils/                 # Utility functions
│   ├── App.vue                # Root component
│   └── main.ts                # Project entry point
├── tests/                     # Unit and integration tests
├── index.html                 # HTML template
├── package.json               # Project metadata and dependencies
├── tsconfig.json              # TypeScript config file
├── vite.config.ts             # Vite config
├── postcss.config.ts          # PostCSS config
└── vitest.config.ts           # Vitest config
```
