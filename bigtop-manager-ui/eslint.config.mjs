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

import { defineConfig, globalIgnores } from 'eslint/config'
import parser from 'vue-eslint-parser'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { FlatCompat } from '@eslint/eslintrc'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const compat = new FlatCompat({
  baseDirectory: __dirname
})

export default defineConfig([
  globalIgnores(['**/node_modules', '**/dist', '**/public', '**/*.d.ts']),
  {
    extends: compat.extends(
      'plugin:vue/vue3-recommended',
      'plugin:@typescript-eslint/recommended',
      'plugin:prettier/recommended',
      'prettier'
    ),

    languageOptions: {
      parser: parser,
      ecmaVersion: 2021,
      sourceType: 'module',

      parserOptions: {
        parser: '@typescript-eslint/parser',

        ecmaFeatures: {
          jsx: true
        }
      }
    },

    rules: {
      '@typescript-eslint/ban-ts-ignore': 'off',
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-var-requires': 'off',
      '@typescript-eslint/no-empty-function': 'off',
      '@typescript-eslint/no-empty-interface': 'off',
      'vue/custom-event-name-casing': 'off',
      'no-use-before-define': 'off',
      '@typescript-eslint/no-use-before-define': 'off',
      '@typescript-eslint/ban-ts-comment': 'off',
      '@typescript-eslint/ban-types': 'off',
      '@typescript-eslint/no-non-null-assertion': 'off',
      '@typescript-eslint/explicit-module-boundary-types': 'off',

      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^(unused|ignored).*$',
          varsIgnorePattern: '^(unused|ignored).*$'
        }
      ],

      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^(unused|ignored).*$',
          varsIgnorePattern: '^(unused|ignored).*$',
          caughtErrorsIgnorePattern: '^(unused|ignored).*$'
        }
      ],

      'prettier/prettier': [
        'error',
        {
          endOfLine: 'auto'
        }
      ],

      'space-before-function-paren': 'off',

      quotes: [
        'error',
        'single',
        {
          avoidEscape: true
        }
      ],

      'comma-dangle': ['error', 'never'],
      'vue/multi-word-component-names': 'off',
      'vue/component-definition-name-casing': 'off',
      'vue/require-valid-default-prop': 'off',
      'vue/no-setup-props-destructure': 'off'
    }
  }
])
