package org.apache.bigtop.manager.server.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class HostPathReq {

    @Schema(example = "[1, 2]")
    private List<Long> hostIds;

    @Schema(example = "/opt")
    private String path;
}
