import type { Plugin } from 'vite'
import { execFile } from 'node:child_process'
import path from 'node:path'
import micromatch from 'micromatch'

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
  const scriptPath = path.resolve('scripts/image-manifest.ts')
  execFile('npx', ['tsx', scriptPath], (error, stdout, stderr) => {
    if (error) {
      console.error('[image-map] error:', error)
      console.error(stderr)
      return
    }
    console.log(stdout)
  })
}
