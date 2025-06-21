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

import forge from 'node-forge'

/**
 * Derives a cryptographic key using PBKDF2 and returns it as a hexadecimal string.
 *
 * @param password - The user's password
 * @param salt - The salt value used in key derivation
 * @returns A hexadecimal string representation of the derived key
 */
export function deriveKey(password: string, salt: string): string {
  const iterations = 600000
  // Key size in bytes: 32 bytes = 256 bits
  const keySizeInBytes = 32

  // Derive key using PBKDF2 with HMAC-SHA256
  const derivedKey = forge.pkcs5.pbkdf2(password, salt, iterations, keySizeInBytes, 'sha256')

  // Convert byte array to hexadecimal string
  return forge.util.bytesToHex(derivedKey)
}
