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

import { describe, it, expect } from 'vitest'
import useSteps from '../../src/composables/use-steps'

describe('useSteps', () => {
  it('should initialize correctly', () => {
    const steps = ['A', 'B', 'C']
    const { current, stepsLimit, hasPrev, hasNext } = useSteps(steps)

    expect(current.value).toBe(0)
    expect(stepsLimit.value).toBe(2)
    expect(hasPrev.value).toBe(false)
    expect(hasNext.value).toBe(true)
  })

  it('should move to the next step if possible', () => {
    const steps = ['A', 'B']
    const { current, nextStep } = useSteps(steps)

    nextStep()
    expect(current.value).toBe(1)

    nextStep()
    expect(current.value).toBe(1)
  })

  it('should move to the previous step if possible', () => {
    const steps = ['A', 'B', 'C']
    const { current, nextStep, previousStep } = useSteps(steps)

    nextStep()
    nextStep()
    previousStep()
    expect(current.value).toBe(1)

    previousStep()
    previousStep()
    expect(current.value).toBe(0)
  })

  it('should handle single-step list correctly', () => {
    const steps = ['A']
    const { current, hasNext, hasPrev, nextStep, previousStep } = useSteps(steps)

    expect(hasNext.value).toBe(false)
    expect(hasPrev.value).toBe(false)

    nextStep()
    expect(current.value).toBe(0)

    previousStep()
    expect(current.value).toBe(0)
  })

  it('should handle empty step list', () => {
    const { current, hasNext, hasPrev, stepsLimit } = useSteps([])

    expect(stepsLimit.value).toBe(-1)
    expect(hasNext.value).toBe(false)
    expect(hasPrev.value).toBe(false)
    expect(current.value).toBe(0)
  })
})
