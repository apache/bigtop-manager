package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

@Data
public class PlatformVO {
    private Long id;

    private String name;

    private String supportModels;

    public PlatformVO(Long i, String name, String models) {
        this.id = i;
        this.name = name;
        this.supportModels = models;
    }
}
