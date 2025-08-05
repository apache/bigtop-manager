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

import { getCurrentUser, updateUser } from '@/api/user'
import { UserReq, UserVO } from '@/api/user/types.ts'

export const useUserStore = defineStore(
  'user',
  () => {
    const userVO = shallowRef<UserVO>()

    const getUserInfo = async () => {
      userVO.value = await getCurrentUser()
    }

    const updateUserInfo = async (editUser: UserReq) => {
      await updateUser(editUser)
      await getUserInfo()
    }

    const logout = async () => {
      userVO.value = undefined
      localStorage.removeItem('Token')
      sessionStorage.removeItem('Token')
    }

    return {
      userVO,
      getUserInfo,
      updateUserInfo,
      logout
    }
  },
  {
    persist: {
      storage: localStorage,
      paths: ['userVO']
    }
  }
)
