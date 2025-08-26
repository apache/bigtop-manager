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
package org.apache.bigtop.manager.grpc.payload;

import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JobCachePayload {

    private Long currentClusterId;

    private Long clusterId;

    private ClusterInfo clusterInfo;

    private Map<String, String> userInfo;

    private List<RepoInfo> repoInfo;

    private Map<String, Map<String, String>> configurations;

    private Map<String, List<String>> componentHosts;

    private List<String> hosts;
}
