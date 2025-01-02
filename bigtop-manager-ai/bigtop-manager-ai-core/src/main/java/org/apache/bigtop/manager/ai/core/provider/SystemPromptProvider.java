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
package org.apache.bigtop.manager.ai.core.provider;

import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;

import java.util.List;

public interface SystemPromptProvider {

    String getSystemMessage(SystemPrompt systemPrompt);

    // return default system prompt
    String getSystemMessage();

    String getLanguagePrompt(String locale);

    String getSystemMessages(List<String> systemPrompts);
}
