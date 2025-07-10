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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.apache.bigtop.manager.agent", "org.apache.bigtop.manager.common"})
public class BigtopManagerAgent {

    public static void main(String[] args) {
        SpringApplication.run(BigtopManagerAgent.class, args);
    }

    @Bean
    @Qualifier("diskMultiGauge") public MultiGauge diskMultiGauge(MeterRegistry meterRegistry) {
        return AgentHostMonitoring.newDiskMultiGauge(meterRegistry);
    }

    @Bean
    @Qualifier("cpuMultiGauge") public MultiGauge cpuMultiGauge(MeterRegistry meterRegistry) {
        return AgentHostMonitoring.newCPUMultiGauge(meterRegistry);
    }

    @Bean
    @Qualifier("memMultiGauge") public MultiGauge memMultiGauge(MeterRegistry meterRegistry) {
        return AgentHostMonitoring.newMemMultiGauge(meterRegistry);
    }

    @Bean
    @Qualifier("diskIOMultiGauge") public MultiGauge diskIOMultiGauge(MeterRegistry meterRegistry) {
        return AgentHostMonitoring.newDiskIOMultiGauge(meterRegistry);
    }
}
