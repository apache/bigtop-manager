package org.apache.bigtop.manager.ai.assistant.store;

import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageRepository;
import org.apache.bigtop.manager.dao.repository.ChatThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;

    public PersistentChatMemoryStore(ChatThreadRepository chatThreadRepository, ChatMessageRepository chatMessageRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
    }
    private ChatMessage convertToChatMessage(ChatMessagePO chatMessagePO) {
        if (chatMessagePO.getSender().equals("AI")) {
            return new AiMessage(chatMessagePO.getMessage());
        } else if (chatMessagePO.getSender().equals("User")) {
            return new UserMessage(chatMessagePO.getMessage());
        } else if (chatMessagePO.getSender().equals("System")) {
            return new SystemMessage(chatMessagePO.getMessage());
        } else {
            return null;
        }
    }

    private ChatMessagePO convertToChatMessagePO(ChatMessage chatMessage, Long chatThreadId) {
        ChatMessagePO chatMessagePO = new ChatMessagePO();
        if (chatMessage.type().equals(ChatMessageType.AI) ){
            chatMessagePO.setSender("AI");
            AiMessage aiMessage = (AiMessage) chatMessage;
            chatMessagePO.setMessage(aiMessage.text());
        } else if (chatMessage.type().equals(ChatMessageType.USER) ){
            chatMessagePO.setSender("User");
            UserMessage userMessage = (UserMessage) chatMessage;
            chatMessagePO.setMessage(userMessage.singleText());
        } else if (chatMessage.type().equals(ChatMessageType.SYSTEM)) {
            chatMessagePO.setSender("System");
            SystemMessage systemMessage = (SystemMessage) chatMessage;
            chatMessagePO.setMessage(systemMessage.text());
        } else {
            chatMessagePO.setSender(chatMessage.type().toString());
        }
        ChatThreadPO chatThreadPO = chatThreadRepository.findById(chatThreadId).orElse(null);
        if (chatThreadPO != null) {
            chatMessagePO.setUserPO(chatThreadPO.getUserPO());
        } else {
            chatMessagePO.setUserPO(null);
        }
        chatMessagePO.setChatThreadPO(chatThreadRepository.findById(chatThreadId).orElse(null));
        return chatMessagePO;
    }

    @Override
    public List<ChatMessage> getMessages(Object threadId) {
        log.info("getMessages called");
        log.info("threadId: {}", threadId.toString());
        ChatThreadPO chatThreadPO = null;
        if (chatThreadRepository != null) {
            chatThreadPO = chatThreadRepository.findById((Long) threadId).orElse(null);
        }
        if (chatThreadPO == null) {
            return new ArrayList<>();
        }
        List<ChatMessagePO> chatMessages = chatMessageRepository.findAllByChatThreadPO(chatThreadPO);
        if(chatMessages.isEmpty()){
            return new ArrayList<>();
        } else {
            return chatMessages.stream()
                    .map(this::convertToChatMessage)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void updateMessages(Object threadId, List<ChatMessage> messages) {
        log.info("updateMessages called");
            ChatMessagePO chatMessagePO = convertToChatMessagePO(messages.get(messages.size()-1),(Long) threadId);
            chatMessageRepository.save(chatMessagePO);
    }

    @Override
    public void deleteMessages(Object threadId) {
        log.info("deleteMessages called");
        ChatThreadPO  chatThreadPO = chatThreadRepository.findById((Long) threadId).orElse(null);
        chatMessageRepository.deleteByChatThreadPO(chatThreadPO);
    }
}