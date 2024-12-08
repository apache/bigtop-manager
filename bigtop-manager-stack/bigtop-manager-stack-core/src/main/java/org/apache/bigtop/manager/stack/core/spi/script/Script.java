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
package org.apache.bigtop.manager.stack.core.spi.script;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.PrioritySPI;
import org.apache.bigtop.manager.stack.core.spi.param.Params;

/**
 * Interface representing a script for component support.
 */
public interface Script extends PrioritySPI {

    /**
     * Install the component.
     *
     * @param params the parameters required for installation
     * @return a ShellResult object containing the result of the installation process
     */
    ShellResult add(Params params);

    /**
     * Configure the component.
     *
     * @param params the parameters required for configuration
     * @return a ShellResult object containing the result of the configuration process
     */
    ShellResult configure(Params params);

    /**
     * Start the component.
     *
     * @param params the parameters required to start the component
     * @return a ShellResult object containing the result of the start process
     */
    ShellResult start(Params params);

    /**
     * Stop the component.
     *
     * @param params the parameters required to stop the component
     * @return a ShellResult object containing the result of the stop process
     */
    ShellResult stop(Params params);

    /**
     * Restart the component.
     *
     * @param params the parameters required to restart the component
     * @return a ShellResult object containing the result of the restart process
     */
    ShellResult restart(Params params);

    /**
     * Check the healthy status of the component.
     * A simple status check which will only check if the process is running
     * use {@link #check(Params)} to check the real healthy status of the component with smoke tests.
     *
     * @param params the parameters required to check the status
     * @return a ShellResult(0 for healthy, -1 for unhealthy) object containing the result of the status check
     */
    ShellResult status(Params params);

    /**
     * Run smoke tests for component to see if it works as expected.
     *
     * @param params the parameters required to check the component
     * @return a ShellResult object indicating success
     */
    ShellResult check(Params params);

    String getComponentName();

    default String getName() {
        return getComponentName();
    }
}
