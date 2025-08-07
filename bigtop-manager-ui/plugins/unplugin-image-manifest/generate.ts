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

import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import fg from 'fast-glob'
import prettier from 'prettier'

async function formatCode(code: string) {
  const options = await prettier.resolveConfig(process.cwd())
  return prettier.format(code, { ...options, parser: 'typescript' })
}

export async function generateMetaMap() {
  const entries = await fg('src/assets/images/**/*.png')

  const lines = entries.map((filePath) => {
    const relativePath = path.relative('src', filePath).replace(/\\/g, '')
    const fileName = path.basename(filePath, '.png')
    return `  '${fileName}': new URL('../${relativePath}', import.meta.url).href,`
  })

  const rawContent = `/*
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
 */\n
const imgMap = {\n${lines.join('\n')}\n}\n\nexport default imgMap\n`

  const formatted = formatCode(rawContent)
  await fs.writeFile('src/utils/img-map.ts', await formatted, 'utf-8')
}
