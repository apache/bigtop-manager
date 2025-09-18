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

export const useModal = () => {
  function confirmModal(payload: ModalFuncProps & { tipText?: string }) {
    return Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', payload.tipText)
        ]),
      style: { top: '30vh' },
      icon: null,
      ...payload
    })
  }

  function destroyAllModal() {
    Modal.destroyAll()
  }

  return {
    confirmModal,
    destroyAllModal
  }
}
