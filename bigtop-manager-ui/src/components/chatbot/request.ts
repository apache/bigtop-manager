export interface SupportedPlatForm {
  id: string | number
  name: string
  supportModels: string
  authCredential: string
}

export interface CredentialFormItem {
  name: string
  displayName: string
}

export interface AuthCredential {
  key: string
  value: string
}

export interface CredentialTest {
  platformId: string | number
  authCredentials: AuthCredential[]
}

export interface ThreadCondition {
  platformId: string | number
  model: string
}
export interface ThreadInfo extends ThreadCondition {
  threadId: string | number
  createTime: string
  updateTime: string
}

export interface Platform {
  id: string | number
  name: string
  supportModels: string
  currModel?: string
}

interface Response<T> {
  code: number
  message: string
  data: T
}

// 获取当前支持的平台列表
export function supportedPlatformList(): Promise<
  Response<SupportedPlatForm[]>
> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '',
        data: [
          {
            id: 1,
            name: 'OpenAI',
            supportModels: 'GPT-3.5,GPT-4o',
            authCredential: 'API Key'
          },
          {
            id: 2,
            name: 'ChatGLM',
            supportModels: 'GPT-3.5,GPT-4o',
            authCredential: 'AppKey'
          }
        ]
      })
    }, 200)
  })
}
/**
 * 新增ai平台的认证信息表单元素
 * @param platformId
 * @returns
 */
export function platformOfCredentialFormModel(
  platformId: string | number
): Promise<Response<CredentialFormItem[]>> {
  console.log('platformId :>> ', platformId)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '',
        data: new Array(4).fill(1).map((_v, idx) => {
          return {
            name: `name${idx + 1}`,
            displayName: `label${idx + 1}`
          }
        })
      })
    }, 200)
  })
}

/**
 * 新增的ai平台授权验证
 * @param params
 * @returns
 */
export function platformOfCredentialTest(
  params: CredentialTest
): Promise<Response<Platform>> {
  console.log('params :>> ', params)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: 'success',
        data: {
          id: 0,
          name: 'test',
          supportModels: 'gpt-3.5-turbo'
        }
      })
    }, 200)
  })
}

export function newThreadCreate(
  params: ThreadCondition
): Promise<Response<ThreadInfo>> {
  console.log('params :>> ', params)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: 'success',
        data: {
          threadId: 0,
          platformId: params.platformId,
          model: params.model,
          createTime: new Date().toISOString(),
          updateTime: new Date().toISOString()
        }
      })
    }, 200)
  })
}

export interface ConnectCondition {
  threadId: number | string
}

export interface Record {
  id: string | number
  object: string
  created: number
  model: string
  choices: Choice[]
  usage: Usage
  threadId: string | number
}

export interface Choice {
  index: number
  message: Message
  finishReason: string
  logprobs: null
}

export interface Message {
  role: string
  content: string
}

export interface Usage {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export function connectAssistant(
  params: ConnectCondition
): Promise<Response<Record>> {
  console.log('params :>> ', params)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: 'success',
        data: {
          id: 0,
          threadId: params.threadId,
          object: 'chat.completion',
          created: 1692872147,
          model: 'gpt-4',
          choices: [
            {
              index: 0,
              message: {
                role: 'assistant',
                content:
                  'Sure, I can help you with that! What specific information are you looking for?'
              },
              finishReason: 'stop',
              logprobs: null
            }
          ],
          usage: {
            promptTokens: 11,
            completionTokens: 17,
            totalTokens: 28
          }
        }
      })
    })
  })
}

interface QuestionItem {
  role: string
  content: string
}
export function sendQuestiontoAssistant(question: QuestionItem) {
  console.log('question :>> ', question)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: 'success',
        data: {
          id: 'chatcmpl-7R9T6T6UvPi4oTVl6dM3w2X5AQi8r',
          object: 'chat.completion',
          created: 1692872147,
          model: 'gpt-4',
          choices: [
            {
              index: 0,
              message: {
                role: 'assistant',
                content:
                  "I'm sorry, I don't have real-time access to weather data. You might want to check a weather service like Weather.com or your local news."
              },
              finish_reason: 'stop'
            },
            {
              index: 1,
              message: {
                role: 'assistant',
                content:
                  "Sure! Why don't scientists trust atoms? Because they make up everything!"
              },
              finish_reason: 'stop'
            },
            {
              index: 2,
              message: {
                role: 'assistant',
                content:
                  "Here's a riddle for you: I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?"
              },
              finish_reason: 'stop'
            }
          ],
          usage: {
            prompt_tokens: 31,
            completion_tokens: 42,
            total_tokens: 73
          }
        }
      })
    })
  })
}
