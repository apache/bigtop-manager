package org.apache.bigtop.manager.server.model.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PlatformReq {
    @NotEmpty
    private Long platformId;

    @NotEmpty
    private String apiKey;
}
