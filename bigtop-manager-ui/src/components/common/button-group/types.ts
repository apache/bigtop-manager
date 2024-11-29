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

import type { VNode } from 'vue'

type BtnType = 'primary' | 'ghost' | 'dashed' | 'link' | 'text' | 'default'
type ShapeType = 'default' | 'circle' | 'round'

export interface GroupItem<T = string> {
  icon?: string | VNode
  tip?: string
  text?: string
  action?: T
  type?: BtnType
  shape?: ShapeType
  clickEvent?: (item?: GroupItem, ...args: any[]) => void
}

export interface Props {
  groups: GroupItem[]
  groupType?: BtnType
  groupShape?: ShapeType
}
