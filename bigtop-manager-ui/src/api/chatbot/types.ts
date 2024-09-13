export interface Platform {
  id: string | number
  name: string
  supportModels: string
  currModel?: string
}

export type AuthorizedPlatform = Platform

export interface SupportedPlatForm extends Platform {
  authCredential: string
}

export interface CredentialFormItem {
  name: string
  displayName: string
}
