export function generateTestData(count = 100) {
  // 定义字段
  const names = ['组件A', '组件B', '组件C', '组件D', '组件E']
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

  // 生成测试数据
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
      componentCount: `${i}个组件`,
      nodeName: nodeNames[Math.floor(Math.random() * nodeNames.length)],
      address: ipAddresses[Math.floor(Math.random() * ipAddresses.length)],
      remark: remarks[Math.floor(Math.random() * remarks.length)],
      status: statuses[Math.floor(Math.random() * statuses.length)]
    })
  }

  return tableData
}

/**
 * 生成随机数据
 * @param {*} stageCount - 需要生成的 stage 数量
 * @param {*} maxTasksPerStage - 每个 stage 中最大任务数量
 * @param {*} maxStatus - 最大 status 值（从 1 开始）
 * @returns {Array} - 生成的数据数组
 */
export function getCheckWorkflows(stageCount: number, maxTasksPerStage: number, maxStatus: number): Array<any> {
  const data = []

  for (let i = 0; i < stageCount; i++) {
    const taskCount = Math.floor(Math.random() * maxTasksPerStage) + 1 // 随机任务数量
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

// Restart ? Required/Not required
// 健康/不健康/未知

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
  return Array.from({ length: 17 }, (_, i) => ({
    key: i,
    serviceName: serviceNames[Math.floor(Math.random() * serviceNames.length)],
    version: `${serviceNames[Math.floor(Math.random() * serviceNames.length)].toLowerCase()}1.0.${i}`,
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
