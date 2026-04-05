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
package org.apache.bigtop.manager.server.command.validator;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.exception.ApiException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validate component CUSTOM command.
 */
@Component
public class ComponentCustomValidator implements CommandValidator {

    @Override
    public List<CommandIdentifier> getCommandIdentifiers() {
        return List.of(new CommandIdentifier(CommandLevel.COMPONENT, Command.CUSTOM));
    }

    @Override
    public void validate(ValidatorContext context) {
        String customCommand = context.getCommandDTO().getCustomCommand();
        if (StringUtils.isBlank(customCommand)) {
            throw new ApiException(ApiExceptionEnum.OPERATION_FAILED, "customCommand must not be blank");
        }
        if (context.getCommandDTO().getComponentCommands() == null
                || context.getCommandDTO().getComponentCommands().isEmpty()) {
            throw new ApiException(ApiExceptionEnum.OPERATION_FAILED, "componentCommands must not be empty");
        }
    }
}
