export enum Sender {
  USER,
  AI,
  SYSTEM
}

export type CreateChatThread = {
  id: number | string | null
  name: string | null
}

export type SenderType = keyof typeof Sender
export type UpdateChatThread = CreateChatThread
export type ThreadId = number | string

export interface ChatThread {
  threadId?: number | string
  authId?: number | string
  platformId?: number | string
  name?: string
  createTime?: string
  updateTime?: string
}

export interface ChatMessageItem {
  sender: SenderType
  message: string
  createTime: string
}
