package org.apache.bigtop.manager.server.model.converter;

import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.model.req.PlatformReq;
import org.apache.bigtop.manager.server.model.req.UserReq;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapStructSharedConfig.class)
public interface PlatformConverter {
    PlatformConverter INSTANCE = Mappers.getMapper(PlatformConverter.class);

    PlatformDTO fromReq2DTO(PlatformReq platformReq);
}
