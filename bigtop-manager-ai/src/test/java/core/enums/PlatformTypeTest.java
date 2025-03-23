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
package org.apache.bigtop.manager.ai.core.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlatformTypeTest {

    @Test
    public void testGetPlatforms() {
        List<String> senders = PlatformType.getPlatforms();
        assertEquals(4, senders.size());
        assertTrue(senders.contains("openai"));
        assertTrue(senders.contains("dashscope"));
        assertTrue(senders.contains("qianfan"));
        assertTrue(senders.contains("deepseek"));
    }

    @Test
    public void testGetPlatformType() {
        assertEquals(PlatformType.OPENAI, PlatformType.getPlatformType("openai"));
        assertEquals(PlatformType.DASH_SCOPE, PlatformType.getPlatformType("dashscope"));
        assertEquals(PlatformType.QIANFAN, PlatformType.getPlatformType("qianfan"));
        assertEquals(PlatformType.DEEPSEEK, PlatformType.getPlatformType("deepseek"));
        assertNull(PlatformType.getPlatformType(""));
        assertNull(PlatformType.getPlatformType(null));
        assertNull(PlatformType.getPlatformType("unknown"));
    }
}
