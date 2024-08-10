package org.apache.bigtop.manager.ai.core.factory;

import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.exception.PlatformNotFoundException;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import java.util.Objects;

/**
 * @Project: org.apache.bigtop.manager.ai.core.factory
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 15:54
 * @Description:
 */
public interface InAIAssistantFactory {


    AIAssistant create(PlatformType platformType, AIAssistantConfigProvider configProvider);

    ToolBox createToolBox(PlatformType platformType);

    default ToolBox createToolBox(String platform){
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if(Objects.isNull(platformType)){
            throw new PlatformNotFoundException(platform);
        }
        return createToolBox(platformType);
    }

    default AIAssistant create(String platform, AIAssistantConfigProvider configProvider){
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if(Objects.isNull(platformType)){
            throw new PlatformNotFoundException(platform);
        }
        return create(platformType,configProvider);
    }



}
