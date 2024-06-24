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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.server.service.CommandLogService;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;

import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandLogServiceImpl implements CommandLogService {

    private final Map<Long, FluxSink<String>> taskSinks = new HashMap<>();

    private final Map<Long, List<String>> logs = new HashMap<>();

    public void registerSink(Long taskId, FluxSink<String> sink) {
        List<String> list = logs.get(taskId);
        if (CollectionUtils.isNotEmpty(list)) {
            synchronized (list) {
                taskSinks.put(taskId, sink);
                for (String log : list) {
                    sink.next(log);
                }
            }
        } else {
            // Task already completed, get logs from database
            sink.next("Task finished, please check the log details on agent machine.");
            sink.complete();
        }
    }

    public void unregisterSink(Long taskId) {
        taskSinks.remove(taskId);
    }

    @Override
    public void onLogStarted(Long taskId, String hostname) {
        logs.put(taskId, new ArrayList<>());
    }

    @Override
    public void onLogReceived(Long taskId, String hostname, String log) {
        List<String> list = logs.get(taskId);

        synchronized (list) {
            list.add(log);
            FluxSink<String> sink = taskSinks.get(taskId);
            if (sink != null) {
                sink.next(log);
            }
        }
    }

    @Override
    public void onLogEnded(Long taskId, String hostname) {
        List<String> list = logs.get(taskId);
        synchronized (list) {
            FluxSink<String> sink = taskSinks.remove(taskId);
            if (sink != null) {
                sink.complete();
            }
        }

        logs.remove(taskId);
    }
}
