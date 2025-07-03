/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { loadEnv, defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'
import Icons from 'unplugin-icons/vite'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import IconsResolver from 'unplugin-icons/resolver'
import { FileSystemIconLoader } from 'unplugin-icons/loaders'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    base: env.VITE_APP_BASE,
    plugins: [
      vue({
        script: {
          defineModel: true
        }
      }),
      Icons({
        compiler: 'vue3',
        customCollections: {
          svg: FileSystemIconLoader('src/assets/images/svg')
        },
        iconCustomizer(collection, icon, props) {
          props.class = (props.class ? props.class + ' ' : '') + 'svg-icon'
        }
      }),
      Components({
        dts: 'src/types/components.d.ts',
        resolvers: [
          IconsResolver({
            customCollections: ['svg'],
            componentPrefix: 'icon'
          })
        ]
      }),
      AutoImport({
        imports: [],
        resolvers: [
          IconsResolver({
            customCollections: ['svg'],
            componentPrefix: 'icon'
          })
        ],
        dts: 'src/types/auto-imports.d.ts'
      })
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@import "@/styles/index.scss";'
        }
      }
    },
    server: {
      hmr: true,
      proxy: {
        [env.VITE_APP_BASE_API]: {
          target: env.VITE_APP_BASE_URL,
          changeOrigin: true
        }
      }
    },
    build: {
      outDir: 'dist',
      sourcemap: true,
      reportCompressedSize: true,
      chunkSizeWarningLimit: 1024,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/name-[hash].[ext]',
          manualChunks(id) {
            if (id.includes('node_modules/lodash-es')) {
              return 'lodash'
            }
          }
        }
      },
      terserOptions: {
        compress: {
          drop_console: true, // remove console
          drop_debugger: true // remove debugger
        }
      }
    }
  }
})
