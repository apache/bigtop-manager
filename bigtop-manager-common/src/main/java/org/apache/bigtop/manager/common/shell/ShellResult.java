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

import org.apache.bigtop.manager.common.constants.MessageConstants;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.MessageFormat;

@Data
@ToString
@NoArgsConstructor
public class ShellResult {

    private Integer exitCode;

    private String output;

    private String errMsg;

    public ShellResult(Integer exitCode, String output, String errMsg) {
        this.exitCode = exitCode;
        this.output = output;
        this.errMsg = errMsg;
    }

    public String getResult() {
        return MessageFormat.format("result=[output={0}, errMsg={1}]", output, errMsg);
    }

    public static ShellResult success(String output) {
        return new ShellResult(MessageConstants.SUCCESS_CODE, output, "");
    }

    public static ShellResult success() {
        return success("Run shell success.");
    }

    public static ShellResult fail(String output) {
        return new ShellResult(MessageConstants.FAIL_CODE, output, "");
    }

    public static ShellResult fail() {
        return fail("Run shell fail.");
    }

    public String formatMessage(String message) {
        return MessageFormat.format(message + ", output: [{0}], err: [{1}]", output, errMsg);
    }
}
