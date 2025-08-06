import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import fg from 'fast-glob'
import prettier from 'prettier'

async function formatCode(code: string) {
  const options = await prettier.resolveConfig(process.cwd())
  return prettier.format(code, { ...options, parser: 'typescript' })
}

async function generateMetaMap() {
  const entries = await fg('src/assets/images/**/*.png')

  const lines = entries.map((filePath) => {
    const relativePath = path.relative('src/assets/generated', filePath).replace(/\\/g, '/')
    const fileName = path.basename(filePath, '.png')
    return `  '${fileName}': new URL('${relativePath}', import.meta.url).href,`
  })

  const rawContent = `const imageMap = {\n${lines.join('\n')}\n}\n\nexport default imageMap\n`

  const formatted = formatCode(rawContent)
  await fs.writeFile('src/assets/generated/image-map.ts', await formatted, 'utf-8')
}

generateMetaMap().catch((err) => {
  console.error(err)
  process.exit(1)
})
