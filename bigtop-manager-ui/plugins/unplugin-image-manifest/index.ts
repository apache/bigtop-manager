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

import type { Plugin } from 'vite'
import path from 'node:path'
import micromatch from 'micromatch'
import { generateMetaMap } from './generate'

interface PluginPayload {
  matchPath: string // watch target path
}

export default function imageManifestPlugin(pluginPayload: PluginPayload): Plugin {
  return {
    name: 'image-manifest',

    configureServer(server) {
      server.watcher.on('add', (filePath) => onAdd(filePath, pluginPayload))
      server.watcher.on('change', (filePath) => onChange(filePath, pluginPayload))
      server.watcher.on('unlink', (filePath) => onUnlink(filePath, pluginPayload))
    }
  }
}

export function isTargetMarkdown(filePath: string, matchPath: string): boolean {
  const relative = path.relative(process.cwd(), filePath)
  return micromatch.isMatch(relative, matchPath)
}

// --------------- watcher actions -----------------

function onAdd(filePath: string, payload: PluginPayload) {
  if (isTargetMarkdown(filePath, payload.matchPath)) {
    updateNoteMeta()
  }
}

function onUnlink(filePath: string, payload: PluginPayload) {
  if (isTargetMarkdown(filePath, payload.matchPath)) {
    updateNoteMeta()
  }
}

async function onChange(filePath: string, payload: PluginPayload) {
  if (isTargetMarkdown(filePath, payload.matchPath)) {
    updateNoteMeta()
  }
}

function updateNoteMeta() {
  generateMetaMap().catch((err: any) => {
    console.error(err)
    process.exit(1)
  })
}
