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
  const ipAddresses = ['192.168.1.1', '192.168.1.2', '192.168.1.3', '192.168.1.4', '192.168.1.5']
  const remarks = ['Main server', 'Backup server', 'Database server', 'Web server', 'Test server']
  const statuses = ['Online', 'Offline', 'Maintenance', 'Active', 'Inactive']

  const tableData = []

  for (let i = 0; i < numRows; i++) {
    tableData.push({
      key: i + 1,
      name: hostnames[Math.floor(Math.random() * hostnames.length)],
      address: ipAddresses[Math.floor(Math.random() * ipAddresses.length)],
      remark: remarks[Math.floor(Math.random() * remarks.length)],
      status: statuses[Math.floor(Math.random() * statuses.length)]
    })
  }

  return tableData
}
