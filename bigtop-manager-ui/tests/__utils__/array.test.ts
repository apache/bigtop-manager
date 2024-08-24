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

import { arrayEquals } from '../../src/utils/array.ts' // 替换成实际的文件路径
import { describe, expect, test } from 'vitest'

describe('arrayEquals', () => {
  test('should return true for equal arrays', () => {
    const arr1 = [1, 2, 3]
    const arr2 = [1, 2, 3]
    expect(arrayEquals(arr1, arr2)).toBe(true)
  })

  test('should return false for arrays of different length', () => {
    const arr1 = [1, 2, 3]
    const arr2 = [1, 2]
    expect(arrayEquals(arr1, arr2)).toBe(false)
  })

  test('should return false for arrays with different items', () => {
    const arr1 = [1, 2, 3]
    const arr2 = [4, 5, 6]
    expect(arrayEquals(arr1, arr2)).toBe(false)
  })

  test('should return true for empty arrays', () => {
    const arr1 = []
    const arr2 = []
    expect(arrayEquals(arr1, arr2)).toBe(true)
  })

  test('should return false if items are not in the same order', () => {
    const arr1 = [1, 2, 3]
    const arr2 = [3, 2, 1]
    expect(arrayEquals(arr1, arr2)).toBe(true)
  })

  test('should return true for arrays with different types of items', () => {
    const arr1 = [1, '2', true]
    const arr2 = [1, '2', true]
    expect(arrayEquals(arr1, arr2)).toBe(true)
  })

  test('should return false if one array includes extra items', () => {
    const arr1 = [1, 2, 3]
    const arr2 = [1, 2, 3, 4]
    expect(arrayEquals(arr1, arr2)).toBe(false)
  })
})
