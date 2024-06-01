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
package org.apache.bigtop.manager.spi.plugin;

import org.springframework.lang.NonNull;

public interface PrioritySPI extends Comparable<Integer> {

    /**
     * The SPI identify, if the two plugin has the same name, will load the high priority.
     * If the priority and name is all same, will throw <code>IllegalArgumentException</code>
     *
     * @return SPI Name
     */
    default String getName() {
        return this.getClass().getName();
    }

    default Integer getPriority() {
        return 0;
    }

    @Override
    default int compareTo(@NonNull Integer o) {
        return Integer.compare(getPriority(), o);
    }

}
