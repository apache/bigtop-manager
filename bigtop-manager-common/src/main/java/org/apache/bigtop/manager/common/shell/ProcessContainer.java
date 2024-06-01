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
package org.apache.bigtop.manager.common.shell;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * process manage container
 */
@Slf4j
public class ProcessContainer extends ConcurrentHashMap<Integer, Process> {

    private static final ProcessContainer container = new ProcessContainer();

    private ProcessContainer() {
        super();
    }

    public static ProcessContainer getInstance() {
        return container;
    }

    public static void putProcess(Process process) {
        getInstance().put(process.hashCode(), process);
    }

    public static int processSize() {
        return getInstance().size();
    }

    public static void removeProcess(Process process) {
        getInstance().remove(process.hashCode());
    }

    public static void destroyAllProcess() {
        Set<Entry<Integer, Process>> set = getInstance().entrySet();
        for (Entry<Integer, Process> entry : set) {
            try {
                entry.getValue().destroy();
            } catch (Exception e) {
                log.error("Destroy All Processes error", e);
            }
        }

        log.info("close " + set.size() + " executing process tasks");
    }
}
