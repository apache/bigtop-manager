import { createApp } from 'vue'

export function withSetup<T>(composable: any, payload?: T) {
  let result: any
  const app = createApp({
    setup() {
      result = composable(payload)
      return () => {}
    }
  })
  app.mount(document.createElement('div'))
  return [result, app]
}
