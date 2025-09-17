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

import { MenuItemProps, MenuProps } from 'ant-design-vue'

type BtnType = 'primary' | 'ghost' | 'dashed' | 'link' | 'text' | 'default'
type ShapeType = 'default' | 'circle' | 'round'

export interface DropdownMenu extends MenuItemProps {
  action: string
  text: string
  divider?: boolean
}
export interface GroupItem<T = any, R = any> {
  icon?: string
  tip?: string
  text?: string
  action?: T
  hidden?: boolean | ((item?: GroupItem<T>, ...payload: R[]) => boolean)
  type?: BtnType
  shape?: ShapeType
  disabled?: boolean
  danger?: boolean
  dropdownMenu?: DropdownMenu[]
  clickEvent?: (item?: GroupItem<T>, ...payload: R[]) => void
  dropdownMenuClickEvent?: (info: Parameters<NonNullable<MenuProps['onClick']>>[0], ...payload: R[]) => void
}

export interface Props {
  i18n?: string
  textCompact?: boolean
  groups: GroupItem[]
  groupType?: BtnType
  groupShape?: ShapeType
  space?: number
  payload?: any
  auto?: boolean
}
