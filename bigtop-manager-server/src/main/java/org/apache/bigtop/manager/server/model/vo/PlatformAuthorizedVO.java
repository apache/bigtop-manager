package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

@Data
public class PlatformAuthorizedVO {
    private Long platformId;

    private String platformName;

    private String apiKey;

    private String supportModels;

    public PlatformAuthorizedVO(long l, String name, String key, String models) {
        this.platformId = l;
        this.platformName = name;
        this.supportModels = models;
        this.apiKey = key;
    }
}
