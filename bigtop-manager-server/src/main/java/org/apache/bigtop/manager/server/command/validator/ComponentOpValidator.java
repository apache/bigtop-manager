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

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ComponentOpValidator implements CommandValidator {

    @Override
    public List<CommandIdentifier> getCommandIdentifiers() {
        return List.of(
                new CommandIdentifier(CommandLevel.COMPONENT, Command.START),
                new CommandIdentifier(CommandLevel.COMPONENT, Command.STOP),
                new CommandIdentifier(CommandLevel.COMPONENT, Command.RESTART));
    }

    @Override
    public void validate(ValidatorContext context) {
        boolean allClient = true;
        List<ComponentCommandDTO> componentCommands = context.getCommandDTO().getComponentCommands();
        for (ComponentCommandDTO componentCommandDTO : componentCommands) {
            String componentName = componentCommandDTO.getComponentName();
            ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);
            if (!Objects.equals(componentDTO.getCategory(), ComponentCategories.CLIENT)) {
                allClient = false;
            }
        }

        if (allClient) {
            throw new ApiException(ApiExceptionEnum.COMPONENT_HAS_NO_SUCH_OP);
        }
    }
}
