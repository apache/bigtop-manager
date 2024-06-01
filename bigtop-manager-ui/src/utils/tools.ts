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

export const copyText = (text: string): Promise<any> => {
  if (navigator.clipboard) {
    return navigator.clipboard.writeText(text)
  }
  return new Promise(async (resolve, reject) => {
    try {
      const { default: ClipboardJS } = await import('clipboard')
      if (!ClipboardJS.isSupported()) {
        reject(new Error('ClipboardJS not support!'))
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
      console.log('copytext :>> ', error)
    }
  })
}
