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
package org.apache.bigtop.manager.ai.dashscope;

import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.enums.MessageSender;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.assistants.Assistant;
import com.alibaba.dashscope.assistants.AssistantParam;
import com.alibaba.dashscope.assistants.Assistants;
import com.alibaba.dashscope.common.GeneralListParam;
import com.alibaba.dashscope.common.ListResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.InvalidateParameter;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.threads.AssistantStreamEvents;
import com.alibaba.dashscope.threads.AssistantThread;
import com.alibaba.dashscope.threads.ContentBase;
import com.alibaba.dashscope.threads.ContentText;
import com.alibaba.dashscope.threads.ThreadParam;
import com.alibaba.dashscope.threads.Threads;
import com.alibaba.dashscope.threads.messages.Messages;
import com.alibaba.dashscope.threads.messages.TextMessageParam;
import com.alibaba.dashscope.threads.messages.ThreadMessage;
import com.alibaba.dashscope.threads.messages.ThreadMessageDelta;
import com.alibaba.dashscope.threads.runs.AssistantStreamMessage;
import com.alibaba.dashscope.threads.runs.Run;
import com.alibaba.dashscope.threads.runs.RunParam;
import com.alibaba.dashscope.threads.runs.Runs;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashScopeAssistant extends AbstractAIAssistant {
    private final Assistants assistants = new Assistants();
    private final Messages messages = new Messages();
    private final Threads threads = new Threads();
    private final Runs runs = new Runs();
    private final DashScopeThreadParam dashScopeThreadParam;

    public DashScopeAssistant(ChatMemory chatMemory, DashScopeThreadParam dashScopeThreadParam) {
        super(chatMemory);
        this.dashScopeThreadParam = dashScopeThreadParam;
    }

    private String getValueFromAssistantStreamMessage(AssistantStreamMessage assistantStreamMessage) {
        ThreadMessageDelta threadMessageDelta = (ThreadMessageDelta) assistantStreamMessage.getData();
        StringBuilder streamMessage = new StringBuilder();

        List<ContentBase> contents = threadMessageDelta.getDelta().getContent();
        for (ContentBase content : contents) {
            ContentText contentText = (ContentText) content;
            streamMessage.append(contentText.getText().getValue());
        }
        return streamMessage.toString();
    }

    private void saveMessage(String message, MessageSender sender) {
        ChatMessage chatMessage;
        if (sender.equals(MessageSender.AI)) {
            chatMessage = new AiMessage(message);
        } else if (sender.equals(MessageSender.USER)) {
            chatMessage = new UserMessage(message);
        } else if (sender.equals(MessageSender.SYSTEM)) {
            chatMessage = new SystemMessage(message);
        } else {
            return;
        }
        chatMemory.add(chatMessage);
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.DASH_SCOPE;
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        if (dashScopeThreadParam.getAssistantId() == null) {
            return;
        }
        TextMessageParam textMessageParam = TextMessageParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .role(Role.ASSISTANT.getValue())
                .content(systemPrompt)
                .build();
        try {
            messages.create(dashScopeThreadParam.getAssistantThreadId(), textMessageParam);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        RunParam runParam = RunParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .assistantId(dashScopeThreadParam.getAssistantId())
                .build();
        try {
            runs.create(dashScopeThreadParam.getAssistantThreadId(), runParam);
        } catch (NoApiKeyException | InputRequiredException | InvalidateParameter e) {
            throw new RuntimeException(e);
        }
        saveMessage(systemPrompt, MessageSender.SYSTEM);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Object getId() {
        return dashScopeThreadParam.getThreadId();
    }

    @Override
    public Flux<String> streamAsk(String userMessage) {
        saveMessage(userMessage, MessageSender.USER);
        TextMessageParam textMessageParam = TextMessageParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();
        try {
            ThreadMessage message = messages.create(dashScopeThreadParam.getAssistantThreadId(), textMessageParam);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }

        RunParam runParam = RunParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .assistantId(dashScopeThreadParam.getAssistantId())
                .stream(true)
                .build();
        Flowable<AssistantStreamMessage> runFlowable = null;
        try {
            runFlowable = runs.createStream(dashScopeThreadParam.getAssistantThreadId(), runParam);
        } catch (NoApiKeyException | InputRequiredException | InvalidateParameter e) {
            throw new RuntimeException(e);
        }
        StringBuilder finalMessage = new StringBuilder();
        return Flux.from(runFlowable)
                .map(assistantStreamMessage -> {
                    String message =
                            assistantStreamMessage.getEvent().equals(AssistantStreamEvents.THREAD_MESSAGE_DELTA)
                                    ? getValueFromAssistantStreamMessage(assistantStreamMessage)
                                    : "";
                    finalMessage.append(message);
                    return message;
                })
                .doOnComplete(() -> {
                    saveMessage(finalMessage.toString(), MessageSender.AI);
                });
    }

    @Override
    public String ask(String userMessage) {
        saveMessage(userMessage, MessageSender.USER);
        TextMessageParam textMessageParam = TextMessageParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();
        try {
            ThreadMessage message = messages.create(dashScopeThreadParam.getAssistantThreadId(), textMessageParam);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }

        RunParam runParam = RunParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .assistantId(dashScopeThreadParam.getAssistantId())
                .build();
        Run run;
        try {
            run = runs.create(dashScopeThreadParam.getAssistantThreadId(), runParam);
        } catch (NoApiKeyException | InputRequiredException | InvalidateParameter e) {
            throw new RuntimeException(e);
        }
        while (true) {
            if (run.getStatus().equals(Run.Status.CANCELLED)
                    || run.getStatus().equals(Run.Status.COMPLETED)
                    || run.getStatus().equals(Run.Status.FAILED)
                    || run.getStatus().equals(Run.Status.REQUIRES_ACTION)
                    || run.getStatus().equals(Run.Status.EXPIRED)) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                run = runs.retrieve(
                        dashScopeThreadParam.getAssistantThreadId(), run.getId(), dashScopeThreadParam.getApiKey());
            } catch (NoApiKeyException e) {
                throw new RuntimeException(e);
            }
        }

        ListResult<ThreadMessage> threadMessages = null;
        try {
            threadMessages = messages.list(
                    dashScopeThreadParam.getAssistantThreadId(),
                    GeneralListParam.builder()
                            .apiKey(dashScopeThreadParam.getApiKey())
                            .build());
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        List<ThreadMessage> threadMessage = threadMessages.getData();
        if (threadMessage.isEmpty()) {
            return null;
        }
        List<ContentBase> contents = threadMessage.get(0).getContent();
        StringBuilder finalMessage = new StringBuilder();
        for (ContentBase content : contents) {
            ContentText contentText = (ContentText) content;
            finalMessage.append(contentText.getText().getValue());
        }
        saveMessage(finalMessage.toString(), MessageSender.AI);
        return finalMessage.toString();
    }

    @Override
    public boolean test() {
        Generation generation = new Generation();
        GenerationParam param = GenerationParam.builder()
                .apiKey(dashScopeThreadParam.getApiKey())
                .model(dashScopeThreadParam.getModel())
                .build();

        Message userMsg =
                Message.builder().role(Role.USER.getValue()).content("1+1=").build();
        param.setMessages(Collections.singletonList(userMsg));
        try {
            generation.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Map<String, String> createThread() {
        AssistantParam param = AssistantParam.builder()
                .model(dashScopeThreadParam.getModel())
                .apiKey(dashScopeThreadParam.getApiKey())
                .name("DashScope Assistant")
                .build();
        Map<String, String> threadInfo = new HashMap<>();
        try {
            Assistant assistant = assistants.create(param);
            threadInfo.put("assistantId", assistant.getId());
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        ThreadParam threadParam =
                ThreadParam.builder().apiKey(dashScopeThreadParam.getApiKey()).build();
        try {
            AssistantThread assistantThread = threads.create(threadParam);
            threadInfo.put("assistantThreadId", assistantThread.getId());
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return threadInfo;
    }

    public static class Builder extends AbstractAIAssistant.Builder {

        public AIAssistant build() {
            String model = ValidationUtils.ensureNotNull(configProvider.getModel(), "model");
            String apiKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("apiKey"), "apiKey");
            DashScopeThreadParam param = new DashScopeThreadParam();
            param.setApiKey(apiKey);
            param.setModel(model);
            String assistantThreadId = configProvider.getConfigs().get("assistantThreadId");
            if (assistantThreadId != null) {
                param.setAssistantThreadId(assistantThreadId);
            }
            String assistantId = configProvider.getConfigs().get("assistantId");
            if (assistantId != null) {
                param.setAssistantId(assistantId);
            }
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(MEMORY_LEN);
            if (id != null) {
                builder.id(id);
                param.setThreadId(id);
            }
            MessageWindowChatMemory chatMemory = builder.build();
            return new DashScopeAssistant(chatMemory, param);
        }
    }
}
