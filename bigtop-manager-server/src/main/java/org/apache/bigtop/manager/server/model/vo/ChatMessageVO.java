package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

@Data
public class ChatMessageVO {
    private String sender;

    private String message;

    private String createTime;

    public ChatMessageVO(String sender, String messageText, String createTime) {
        this.sender = sender;
        this.message = messageText;
        this.createTime = createTime;
    }
}
