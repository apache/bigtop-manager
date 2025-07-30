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

import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

// Transform pattern expression hostNames
const replacePatternInHosts = (rawHostNames: string[]): string[] => {
  let start: any,
    end: any,
    extra: any,
    allHostNames: any = []
  rawHostNames.forEach(function (rawHostName) {
    const hostNames = [] as Array<string>
    start = rawHostName.match(/\[\d*/)
    end = rawHostName.match(/\-\d*]/)
    extra = { 0: '' }

    start = start[0].substring(1)
    end = end[0].substring(1)

    if (parseInt(start) <= parseInt(end, 10) && parseInt(start, 10) >= 0) {
      if (start[0] == '0' && start.length > 1) {
        extra = start.match(/0*/)
      }

      for (let i = parseInt(start, 10); i < parseInt(end, 10) + 1; i++) {
        hostNames.push(
          rawHostName.replace(/\[\d*\-\d*\]/, extra[0].substring(0, start.length - i.toString().length) + i)
        )
      }
    } else {
      hostNames.push(rawHostName)
    }
    allHostNames = allHostNames.concat(hostNames)
  })

  return allHostNames
}

// parse hostNames as pattern expression
export const parseHostNamesAsPatternExpression = (hostNameStr: string): string[] => {
  let hostNames: string[] = []
  const hostNameArr = hostNameStr.replace(/\n|\r\n|\s/g, ',').split(',')
  hostNameArr.forEach((a) => {
    const allPatterns = a.match(/\[\d*\-\d*\]/g)
    const patternsNumber: number = allPatterns ? allPatterns.length : 0
    let hn = [a]
    if (patternsNumber) {
      for (let i = 0; i < patternsNumber; i++) {
        hn = replacePatternInHosts(hn)
      }
      hostNames = hostNames.concat(hn)
    } else {
      hostNames.push(a)
    }
  })
  return Array.from(new Set(hostNames))
}

dayjs.extend(utc)
dayjs.extend(timezone)
export const formatTime = (time: string | undefined, formatRule: string = 'YYYY-MM-DD HH:mm:ss') => {
  return typeof time === 'string' && dayjs(time).tz(dayjs.tz.guess(), true).format(formatRule)
}
