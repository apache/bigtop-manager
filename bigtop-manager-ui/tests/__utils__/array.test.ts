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
