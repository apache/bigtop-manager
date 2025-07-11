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

/**
 * Creates a Map of base series configuration for ECharts based on a name mapping.
 *
 * @param nameMap - An array of tuples mapping series keys to display names.
 * @returns A Map where each key maps to an ECharts line series config object with empty `data`.
 */
export const createSeriesForChart = (nameMap: [string, string][]) => {
  const baseConfig = {
    type: 'line',
    stack: 'Total'
  }

  const map = new Map()
  for (const [key, name] of nameMap) {
    map.set(key, { name, ...baseConfig, data: [] })
  }

  return map
}

/**
 * Fills series data from a given data object based on a legend mapping.
 *
 * @param data - A partial object containing data arrays for each series key.
 * @param legendMap - An array of [key, displayName] pairs for series.
 * @returns An array of series config objects with populated data for use in ECharts.
 */
export const formatSeriesData = <T>(data: Partial<T>, legendMap: [string, string][]) => {
  const series = createSeriesForChart(legendMap)

  for (const [key, value] of series) {
    value.data = data[key]
  }

  return Array.from(series.values())
}

/**
 * Rounds a number to a fixed number of decimal places.
 *
 * @param num - The number to round.
 * @param decimals - The number of decimal places to keep (default is 2).
 * @returns The rounded number.
 */
export const roundFixed = (num: number, decimals = 2) => {
  const factor = 10 ** decimals
  return Math.round((num + Number.EPSILON) * factor) / factor
}
