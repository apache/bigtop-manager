package org.apache.bigtop.manager.server.model.vo;

import org.apache.bigtop.manager.server.enums.InstalledStatusEnum;

import lombok.Data;

@Data
public class InstalledStatusVO {

    private String hostname;

    private InstalledStatusEnum status;

    private String message;
}
