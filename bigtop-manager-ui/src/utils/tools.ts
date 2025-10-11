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

/**
 * Copies the given text to the clipboard.
 * Uses the Clipboard API if available, otherwise falls back to ClipboardJS.
 *
 * @param text - The text to copy.
 * @returns A promise that resolves on success or rejects on failure.
 */
export function copyText(text: string): Promise<any> {
  if (navigator.clipboard) {
    return navigator.clipboard.writeText(text)
  }
  return new Promise(async (resolve, reject) => {
    try {
      const { default: ClipboardJS } = await import('clipboard')
      if (!ClipboardJS.isSupported()) {
        reject(new Error('ClipboardJS not supported!'))
        return
      }
      const btn = document.createElement('button')
      btn.innerText = text
      const clipboard = new ClipboardJS(btn, {
        text: () => text
      })
      clipboard.on('success', () => {
        resolve(true)
        clipboard.destroy()
      })
      clipboard.on('error', (err) => {
        reject(err)
        clipboard.destroy()
      })
      btn.click()
    } catch (error) {
      console.log('copyText :>> ', error)
    }
  })
}

/**
 * Returns the URL of a PNG image from the assets folder.
 *
 * @param imageName - The name of the image (default is 'logo').
 * @returns The URL of the image.
 */
export function usePngImage(imageName: string = 'logo'): string {
  return new URL(`../assets/images/${imageName}.png`, import.meta.url).href
}

/**
 * Scrolls the given container element to the bottom.
 *
 * @param container - The container element to scroll.
 */
export function scrollToBottom(container: HTMLElement | null) {
  if (!container) {
    return
  }
  requestAnimationFrame(() => {
    container.scrollTop = container.scrollHeight
  })
}

/**
 * Generates a random number string based on the current timestamp.
 *
 * @param len - The length of the random string (default is 6).
 * @returns A random string derived from the timestamp.
 */
export function getRandomFromTimestamp(len: number = 6) {
  return Date.now().toString().slice(-len)
}

/**
 * Generates a random alphanumeric string of the specified length.
 *
 * @param length - The length of the random string (default is 8).
 * @returns A random alphanumeric string.
 */
export function generateRandomId(length = 8) {
  return Math.random()
    .toString(36)
    .substring(2, length + 2)
}

/**
 * Picks specific keys from an object and returns a new object with those keys.
 *
 * @param obj - The source object.
 * @param keys - The keys to pick from the object.
 * @returns A new object with the picked keys.
 */
export function pick<T extends object, K extends keyof T>(obj: T, keys: K[]): Pick<T, K> {
  return keys.reduce(
    (acc, key) => {
      if (obj.hasOwnProperty(key)) {
        acc[key] = obj[key]
      }
      return acc
    },
    {} as Pick<T, K>
  )
}

/**
 * Creates a new object with a unique key added to it.
 *
 * @param item - The source object.
 * @returns A new object with an added `__key` property.
 */
export function createKeyedItem<T extends object>(item: T): T & { __key: string } {
  return {
    ...item,
    __key: crypto.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
  }
}

/**
 * Checks if a value is empty (undefined, null, or an empty string).
 *
 * @param value - The value to check.
 * @returns True if the value is empty, otherwise false.
 */
export function isEmpty<T>(value: T): boolean {
  return value === undefined || value === null || value === ''
}
