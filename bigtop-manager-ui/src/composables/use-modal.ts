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

import SvgIcon from '@/components/base/svg-icon/index.vue'
import { Modal, ModalFuncProps } from 'ant-design-vue'

interface ConfirmModalProps extends ModalFuncProps {
  tipText?: string
}

const DEFAULT_STYLE = { top: '30vh' }

export const useModal = () => {
  function confirmModal({ tipText, onOk, ...rest }: ConfirmModalProps) {
    return Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', tipText ?? '')
        ]),
      style: DEFAULT_STYLE,
      icon: null,
      ...rest,
      onOk: async () => {
        try {
          if (onOk) {
            await onOk()
          }
        } catch (e) {
          console.error('Modal onOk error:', e)
        }
        return Promise.resolve()
      }
    })
  }

  return {
    confirmModal,
    destroyAllModal: Modal.destroyAll
  }
}
