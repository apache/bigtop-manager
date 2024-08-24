// Configure Vitest (https://vitest.dev/config/)
import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

export default defineConfig((configEnv) =>
  mergeConfig(
    viteConfig(configEnv),
    defineConfig({
      test: {
        globals: true, // Open global testing API similar to jest
        include: ['**/tests/**/*.test.ts'], //  Match directory structure
        environment: 'happy-dom' // Use happy-dom to simulate DOM
      }
    })
  )
)
