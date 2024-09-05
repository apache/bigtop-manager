package org.apache.bigtop.manager.ai.assistant.store;

import org.apache.bigtop.manager.ai.core.enums.MessageSender;
import org.apache.bigtop.manager.ai.dashscope.repository.DashScopeMessageRepository;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

public class PersistentRepository implements DashScopeMessageRepository {
    private final ChatThreadDao chatThreadDao;
    private final ChatMessageDao chatMessageDao;

    private boolean noPersistent() {
        return chatThreadDao == null || chatMessageDao == null;
    }

    public PersistentRepository(ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        this.chatThreadDao = chatThreadDao;
        this.chatMessageDao = chatMessageDao;
    }

    public PersistentRepository() {
        this.chatThreadDao = null;
        this.chatMessageDao = null;
    }

    private ChatMessagePO getChatMessagePO(String message, Long threadId, MessageSender sender) {
        if (noPersistent()) {
            return null;
        }
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        ChatMessagePO chatMessagePO = new ChatMessagePO();
        chatMessagePO.setUserId(chatThreadPO.getUserId());
        chatMessagePO.setThreadId(threadId);
        chatMessagePO.setSender(sender.getValue());
        chatMessagePO.setMessage(message);
        return chatMessagePO;
    }

    @Override
    public void saveUserMessage(String message, Long threadId) {
        if (noPersistent()) {
            return;
        }
        ChatMessagePO chatMessagePO = getChatMessagePO(message, threadId, MessageSender.USER);
        chatMessageDao.save(chatMessagePO);
    }

    @Override
    public void saveAiMessage(String message, Long threadId) {
        if (noPersistent()) {
            return;
        }
        ChatMessagePO chatMessagePO = getChatMessagePO(message, threadId, MessageSender.AI);
        chatMessageDao.save(chatMessagePO);
    }

    @Override
    public void saveSystemMessage(String message, Long threadId) {
        if (noPersistent()) {
            return;
        }
        ChatMessagePO chatMessagePO = getChatMessagePO(message, threadId, MessageSender.SYSTEM);
        chatMessageDao.save(chatMessagePO);
    }
}
