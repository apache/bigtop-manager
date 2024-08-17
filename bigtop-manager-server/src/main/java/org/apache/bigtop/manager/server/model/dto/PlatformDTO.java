package org.apache.bigtop.manager.server.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PlatformDTO {
    private Long platformId;
    private String apiKey;
}
