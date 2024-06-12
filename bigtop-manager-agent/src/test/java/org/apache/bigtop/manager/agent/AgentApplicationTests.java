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
package org.apache.bigtop.manager.agent;

import org.apache.bigtop.manager.agent.monitoring.AgentHostMonitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.UnknownHostException;
import java.text.DecimalFormat;

@SpringBootTest
public class AgentApplicationTests {

    @Test
    void contextLoads() {}

    @Test
    void getHostAgentInfo() throws UnknownHostException {
        JsonNode hostInfo = AgentHostMonitoring.getHostInfo();
        System.out.println(hostInfo.toPrettyString());
    }

    @Test
    void testNum() {
        System.out.println(new DecimalFormat("#.00").format(123.2344));
    }
}
