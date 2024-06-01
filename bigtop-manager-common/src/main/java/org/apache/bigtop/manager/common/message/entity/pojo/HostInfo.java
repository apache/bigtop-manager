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
package org.apache.bigtop.manager.common.message.entity.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class HostInfo implements Serializable {

    private String hostname;

    private String ipv4;

    private String ipv6;

    private String os;

    private String version;

    private String arch;

    private BigDecimal cpuLoad;

    private Integer availableProcessors;

    private BigDecimal processCpuLoad;

    private Long processCpuTime;

    private Long totalMemorySize;

    private Long freeMemorySize;

    private Long totalSwapSpaceSize;

    private Long freeSwapSpaceSize;

    private Long committedVirtualMemorySize;

    private BigDecimal systemLoadAverage;

    private Long freeDisk;

    private Long totalDisk;
}
