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
package org.apache.bigtop.manager.common.utils.shell;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;

import java.util.ArrayList;
import java.util.List;

public class ShellExecutorTests {

    public static void main(String[] args) throws Exception {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("cmd");
        builderParameters.add("/c");
        builderParameters.add(
                "E:\\Projects\\GitHub\\bigtop-manager\\bigtop-manager-common\\src\\test\\java\\org\\apache\\bigtop\\manager\\common\\utils\\shell\\test.bat");

        List<String> res = new ArrayList<>();
        ShellResult shellResult = ShellExecutor.execCommand(builderParameters, System.out::println);
        System.out.println("-----------");
        System.out.println(shellResult.getResult());
    }
}
