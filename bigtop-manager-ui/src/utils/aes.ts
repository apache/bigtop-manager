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

import CryptoJS from 'crypto-js'

export class AESUtils {
  /**
   * AES ECB encryption (returns Base64 string)
   */
  public static encrypt(str: string, keyStr: string): string {
    this.validateParams(str, keyStr)
    const key = CryptoJS.enc.Utf8.parse(keyStr)
    const encrypted = CryptoJS.AES.encrypt(str, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    })
    return encrypted.toString()
  }

  /**
   * AES ECB decryption (accepts Base64 string)
   */
  public static decrypt(encryptedStr: string, keyStr: string): string {
    this.validateParams(encryptedStr, keyStr)
    const key = CryptoJS.enc.Utf8.parse(keyStr)
    const decrypted = CryptoJS.AES.decrypt(encryptedStr, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    })
    return decrypted.toString(CryptoJS.enc.Utf8)
  }

  /**
   * Parameter validation
   */
  private static validateParams(str: string, key: string): void {
    if (!str) {
      throw new Error('str cannot be empty')
    }
    if (!key) {
      throw new Error('key cannot be empty')
    }
    const keyLength = key.length
    if (keyLength !== 16 && keyLength !== 32) {
      throw new Error('Key length must be 16 characters (128 bits) or 32 characters (256 bits)')
    }
  }
}
