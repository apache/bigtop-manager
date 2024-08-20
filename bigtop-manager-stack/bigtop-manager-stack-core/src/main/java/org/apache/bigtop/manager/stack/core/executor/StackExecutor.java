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
package org.apache.bigtop.manager.stack.core.executor;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.CustomCommandInfo;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.CaseUtils;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.spi.PrioritySPIFactory;
import org.apache.bigtop.manager.stack.core.spi.hook.Hook;
import org.apache.bigtop.manager.stack.core.spi.script.Script;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Slf4j
public class StackExecutor {

    private static final Map<String, Script> SCRIPT_MAP = new PrioritySPIFactory<>(Script.class).getSPIMap();

    private static final Map<String, Hook> HOOK_MAP = new PrioritySPIFactory<>(Hook.class).getSPIMap();

    private static Script getCommandScript(String scriptId) {
        Script script = SCRIPT_MAP.get(scriptId);
        if (script == null) {
            throw new StackException("Cannot find Script Class {0}", scriptId);
        }

        return script;
    }

    private static Script getCustomScript(String customCommand, List<CustomCommandInfo> customCommands) {
        Script script = null;
        for (CustomCommandInfo customCommandInfo : customCommands) {
            if (customCommandInfo.getName().equals(customCommand)) {
                script = getCommandScript(customCommandInfo.getCommandScript().getScriptId());
            }
        }

        if (script == null) {
            throw new StackException("Cannot find script class {0}", customCommand);
        }

        return script;
    }

    private static void runBeforeHook(String command, Params params) {
        Hook hook = HOOK_MAP.get(command.toLowerCase());
        if (hook != null) {
            hook.before(params);
        }
    }

    private static void runAfterHook(String command, Params params) {
        Hook hook = HOOK_MAP.get(command.toLowerCase());
        if (hook != null) {
            hook.after(params);
        }
    }

    public static ShellResult execute(CommandPayload commandPayload) {
        try {
            String command;
            Script script;
            if (commandPayload.getCommand().name().equals(Command.CUSTOM.name())) {
                command = commandPayload.getCustomCommand();
                script = getCustomScript(command, commandPayload.getCustomCommands());
            } else {
                command = commandPayload.getCommand().name();
                script = getCommandScript(commandPayload.getCommandScript().getScriptId());
            }

            String methodName = CaseUtils.toCamelCase(command, CaseUtils.SEPARATOR_UNDERSCORE, false);
            Method method = script.getClass().getMethod(methodName, Params.class);

            String paramsClassName = script.getClass().getPackageName() + "."
                    + CaseUtils.toCamelCase(commandPayload.getServiceName()) + "Params";
            Class<?> paramsClass = Class.forName(paramsClassName);
            Params params = (Params)
                    paramsClass.getDeclaredConstructor(CommandPayload.class).newInstance(commandPayload);

            if (Environments.isDevMode()) {
                log.info("Executing {}::{} on dev mode", script.getName(), method.getName());
                return ShellResult.success();
            } else {
                runBeforeHook(command, params);

                log.info("Executing {}::{}", script.getName(), method.getName());
                ShellResult result = (ShellResult) method.invoke(script, params);
                if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
                    log.error("Error executing script: {}", result.getErrMsg());
                }

                runAfterHook(command, params);

                return result;
            }
        } catch (Exception e) {
            log.error("Error executing command, payload: {}", commandPayload, e);
            return ShellResult.fail();
        }
    }
}
