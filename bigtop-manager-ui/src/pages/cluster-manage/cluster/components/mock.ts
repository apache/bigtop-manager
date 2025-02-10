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

export function generateTestData(count = 100) {
  const names = ['componentA', 'componentB', 'componentC', 'componentD', 'componentE']
  const versions = ['1.0.0', '2.3.1', '3.2.4', '4.0.0', '5.1.2']
  const compStacks = [
    'React, Redux, Webpack',
    'Vue, Vuex, Vite',
    'Angular, RxJS, NgRx',
    'Node.js, Express, MongoDB',
    'Svelte, Rollup, Firebase'
  ]
  const descriptions = [
    '这是一个用于状态管理的组件',
    '这是一个用于数据展示的组件',
    '这是一个用于实时数据处理的组件',
    '这是一个后端服务组件',
    '这是一个前端开发框架'
  ]

  const testData = Array.from({ length: count }, (_, i) => ({
    key: (i + 1).toString(),
    name: names[Math.floor(Math.random() * names.length)],
    version: versions[Math.floor(Math.random() * versions.length)],
    compStack: compStacks[Math.floor(Math.random() * compStacks.length)],
    descrip: descriptions[Math.floor(Math.random() * descriptions.length)]
  }))

  return testData
}

export function generateTableHostData(numRows = 100) {
  const hostnames = ['Server-A', 'Server-B', 'Server-C', 'Server-D', 'Server-E']
  const nodeNames = ['host-A', 'host-B', 'host-C', 'host-D']
  const ipAddresses = ['192.168.1.1', '192.168.1.2', '192.168.1.3', '192.168.1.4', '192.168.1.5']
  const systems = 'Rocky Linux 8.8'
  const architecture = 'x86_64'
  const remarks = ['Main server', 'Backup server', 'Database server', 'Web server', 'Test server']
  const statuses = ['installing', 'success', 'error', 'unknow']

  const tableData = []

  for (let i = 0; i < numRows; i++) {
    tableData.push({
      key: i + 1,
      name: hostnames[Math.floor(Math.random() * hostnames.length)],
      system: `${systems}-${i}`,
      architecture: `${architecture}-${i}`,
      componentCount: `${i} components`,
      nodeName: nodeNames[Math.floor(Math.random() * nodeNames.length)],
      address: ipAddresses[Math.floor(Math.random() * ipAddresses.length)],
      remark: remarks[Math.floor(Math.random() * remarks.length)],
      status: statuses[Math.floor(Math.random() * statuses.length)]
    })
  }

  return tableData
}

export function getCheckWorkflows(stageCount: number, maxTasksPerStage: number, maxStatus: number): Array<any> {
  const data = []

  for (let i = 0; i < stageCount; i++) {
    const taskCount = Math.floor(Math.random() * maxTasksPerStage) + 1
    const tasks = []

    for (let j = 0; j < taskCount; j++) {
      tasks.push({
        id: j,
        name: `task${j + 1}`,
        status: Math.floor(Math.random() * maxStatus) + 1
      })
    }

    data.push({
      id: i,
      name: `stage${i + 1}`,
      status: Math.floor(Math.random() * maxStatus) + 1,
      tasks
    })
  }

  return data
}

export enum StatusColors {
  success = 'success',
  error = 'error',
  unknow = 'warning'
}

export enum StatusTexts {
  success = 'healthy',
  error = 'unhealthy',
  unknow = 'unknown'
}

export type ServiceStatus = keyof typeof StatusColors

export interface ServiceItem {
  key: string | number
  serviceName: string
  version: string
  restart: boolean
  status: ServiceStatus
}

export function getServices(): ServiceItem[] {
  const statusList = ['success', 'error', 'unknow']
  const serviceNames = [
    'GraFana',
    'Flink',
    'Kafka',
    'ZooKeeper',
    'Hadoop',
    'Hbase',
    'Hive',
    'MySQL',
    'Spark',
    'Solr',
    'Seatunnel',
    'Tez',
    'Prometheus'
  ]
  return Array.from({ length: serviceNames.length }, (_, i) => ({
    key: i,
    serviceName: serviceNames[i],
    version: `${serviceNames[i].toLowerCase()}1.0.${i}`,
    restart: Math.floor(Math.random() * 2) == 0,
    status: statusList[Math.floor(Math.random() * statusList.length)] as ServiceStatus
  }))
}

export interface UserListItem {
  serviceName: string
  userName: string
  userGroup: string
  descrip: string
}

export function getUserList(count: number = 20): UserListItem[] {
  return Array.from({ length: count }, (_, i) => ({
    key: i,
    serviceName: `serviceName-${i}`,
    userName: `user-${i}`,
    userGroup: `userGroup-${i}`,
    descrip: 'descrip-descrip-descrip'
  }))
}

type JobStatus = 'success' | 'exception' | 'normal' | 'active'
export interface JobListItem {
  name: string
  status: JobStatus
  progress: number
  createTime: string
  updateTime: string
}

export function getJobList(count: number = 20): JobListItem[] {
  const status = ['success', 'exception', 'normal', 'active']
  return Array.from({ length: count }, (_, i) => ({
    key: i,
    name: `name-${i}`,
    progress: Math.floor(Math.random() * 100),
    status: status[Math.floor(Math.random() * status.length)] as JobStatus,
    createTime: '2024-09-19 11:11:11',
    updateTime: '2024-09-19 11:11:11'
  }))
}
