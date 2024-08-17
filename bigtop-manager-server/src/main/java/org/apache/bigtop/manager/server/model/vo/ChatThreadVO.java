package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

@Data
public class ChatThreadVO {
    private Long threadId;

    private Long platformId;

    private String model;

    private String createTime;

    private String updateTime;

    public ChatThreadVO(Long tid, Long pid, String model, String createTime) {
        this.threadId = tid;
        this.platformId = pid;
        this.model = model;
        this.createTime = createTime;
        this.updateTime = createTime;
    }
}
