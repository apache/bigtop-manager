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

export const formatFromByte = (value: number, decimals = 2): string => {
  if (isNaN(value)) {
    return ''
  }

  if (value < 1024) {
    return `${value} B`
  } else if (value < 1024 ** 2) {
    return `${(value / 1024).toFixed(decimals)} KB`
  } else if (value < 1024 ** 3) {
    return `${(value / 1024 ** 2).toFixed(decimals)} MB`
  } else if (value < 1024 ** 4) {
    return `${(value / 1024 ** 3).toFixed(decimals)} GB`
  } else if (value < 1024 ** 5) {
    return `${(value / 1024 ** 4).toFixed(decimals)} TB`
  } else {
    return `${(value / 1024 ** 5).toFixed(decimals)} PB`
  }
}

/**
 * Safely rounds a value to a fixed number of decimal places.
 *
 * @param num - The value to round.
 * @param decimals - Decimal places to keep (default: 2).
 * @param fallback - Fallback string if value is not finite (default: '0.00').
 * @param preserveEmpty - If true, returns null or '' as-is; otherwise, falls back (default: true).
 * @returns Rounded string, fallback, or original empty/null input.
 */
export const roundFixed = (
  num: unknown,
  decimals = 2,
  fallback = '0.00',
  preserveEmpty = true
): string | null | undefined => {
  if (preserveEmpty && (num === '' || num === null || num === undefined)) {
    return num
  }

  const n = Number(num)
  if (!isFinite(n)) return fallback

  const factor = 10 ** decimals
  const rounded = Math.round((n + Number.EPSILON) * factor) / factor
  return rounded.toFixed(decimals)
}
