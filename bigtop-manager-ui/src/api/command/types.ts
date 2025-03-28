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

export interface CommandRequest {
  clusterCommand?: ClusterCommandReq
  clusterId?: number
  command: keyof typeof Command
  commandLevel: keyof typeof CommandLevel
  componentCommands?: ComponentCommandReq[]
  customCommand?: string
  hostCommands?: HostCommandReq[]
  serviceCommands?: ServiceCommandReq[]
  [property: string]: any
}

export interface ClusterCommandReq {
  desc?: string
  displayName: string
  hosts: HostReq[]
  name: string
  rootDir?: string
  type: number
  userGroup?: string
  [property: string]: any
}

export interface HostReq {
  agentDir?: string
  authType?: number | string
  clusterId?: number
  desc?: string
  grpcPort?: number
  hostnames?: string[]
  sshKeyFilename?: string
  sshKeyPassword?: string
  sshKeyString?: string
  sshPassword?: string
  sshPort?: number
  sshUser?: string
  [property: string]: any
}

export enum Command {
  Add = 'Add',
  Check = 'Check',
  Configure = 'Configure',
  Custom = 'Custom',
  Init = 'Init',
  Prepare = 'Prepare',
  Restart = 'Restart',
  Start = 'Start',
  Status = 'Status',
  Stop = 'Stop',
  More = 'More'
}

export enum CommandLevel {
  cluster = 'cluster',
  component = 'component',
  host = 'host',
  service = 'service'
}

export interface ComponentCommandReq {
  componentName: string
  hostnames: string[]
  [property: string]: any
}

export interface HostCommandReq {
  agentDir?: string
  authType?: number
  clusterId?: number
  desc?: string
  grpcPort?: number
  hostnames?: string[]
  sshKeyFilename?: string
  sshKeyPassword?: string
  sshKeyString?: string
  sshPassword?: string
  sshPort?: number
  sshUser?: string
  [property: string]: any
}

export interface ServiceCommandReq {
  componentHosts?: ComponentHostReq[]
  configs?: ServiceConfigReq[]
  serviceName: string
  [property: string]: any
}

export interface ComponentHostReq {
  componentName: string
  hostnames: string[]
  [property: string]: any
}

export interface ServiceConfigReq {
  id?: number
  name?: string
  properties: PropertyReq[]
  [property: string]: any
}

export interface PropertyReq {
  attrs?: AttrsReq
  desc?: string
  displayName?: string
  name: string
  value?: string
  [property: string]: any
}

export interface AttrsReq {
  type?: string
  [property: string]: any
}

export interface CommandVO {
  id?: number
  state?: string
  [property: string]: any
}
