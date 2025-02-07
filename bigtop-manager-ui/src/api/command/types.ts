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
 * CommandReq
 */
export interface CommandRequest {
  clusterCommand?: ClusterCommandReq
  clusterId?: number
  command: Command
  commandLevel: CommandLevel
  /**
   * Command details for component level command
   */
  componentCommands?: ComponentCommandReq[]
  customCommand?: string
  /**
   * Command details for host level command
   */
  hostCommands?: HostCommandReq[]
  /**
   * Command details for service level command
   */
  serviceCommands?: ServiceCommandReq[]
  [property: string]: any
}

export type CommandRequestKeys = keyof CommandRequest

/**
 * ClusterCommandReq，Command details for cluster level command
 */
export interface ClusterCommandReq {
  desc?: string
  displayName: string
  /**
   * Hosts info for this cluster
   */
  hosts: HostReq[]
  name: string
  rootDir?: string
  type: number
  userGroup?: string
  [property: string]: any
}

/**
 * HostReq
 */
export interface HostReq {
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
  Stop = 'Stop'
}

export enum CommandLevel {
  Cluster = 'cluster',
  Component = 'component',
  Host = 'host',
  Service = 'service'
}

/**
 * ComponentCommandReq，Command details for component level command
 */
export interface ComponentCommandReq {
  /**
   * Component Name
   */
  componentName: string
  /**
   * Hostnames for component
   */
  hostnames: string[]
  [property: string]: any
}

/**
 * HostCommandReq，Command details for host level command
 */
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

/**
 * ServiceCommandReq，Command details for service level command
 */
export interface ServiceCommandReq {
  /**
   * Components for service on each hosts
   */
  componentHosts: ComponentHostReq[]
  /**
   * Configs for service
   */
  configs: ServiceConfigReq[]
  /**
   * Service name
   */
  serviceName: string
  [property: string]: any
}

/**
 * ComponentHostReq，Components for service on each hosts
 */
export interface ComponentHostReq {
  /**
   * Component name
   */
  componentName: string
  /**
   * Hostnames for component
   */
  hostnames: string[]
  [property: string]: any
}

/**
 * ServiceConfigReq，Configs for service
 */
export interface ServiceConfigReq {
  id?: number
  name?: string
  properties?: PropertyReq[]
  [property: string]: any
}

/**
 * PropertyReq
 */
export interface PropertyReq {
  attrs?: AttrsReq
  desc?: string
  displayName?: string
  name: string
  value?: string
  [property: string]: any
}

/**
 * AttrsReq
 */
export interface AttrsReq {
  type?: string
  [property: string]: any
}
