package org.apache.bigtop.manager.ai.core.factory;

import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.exception.PlatformNotFoundException;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import java.util.Objects;
import java.util.UUID;


/**
 * @Project: org.apache.bigtop.manager.ai.core.factory
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 15:54
 * @Description:
 */
public interface AIAssistantAbstractFactory {


    AIAssistant createWithPrompt(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id, Object promptId);

    AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id);

    ToolBox createToolBox(PlatformType platformType);



    default AIAssistant createWithPrompt(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object prompt){
        return createWithPrompt(platformType,assistantConfig,UUID.randomUUID().toString(),prompt);
    }

    default AIAssistant create(String platform, AIAssistantConfigProvider assistantConfigProvider,Object id){
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if(Objects.isNull(platformType)){
            throw new PlatformNotFoundException(platform);
        }
        return create(platformType,assistantConfigProvider,id);
    }

    default AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfigProvider){
        return create(platformType,assistantConfigProvider,UUID.randomUUID().toString());
    }

    default AIAssistant create(String platform, AIAssistantConfigProvider assistantConfig){
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if(Objects.isNull(platformType)){
            throw new PlatformNotFoundException(platform);
        }
        return create(platformType,assistantConfig);
    }

    default ToolBox createToolBox(String platform){
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if(Objects.isNull(platformType)){
            throw new PlatformNotFoundException(platform);
        }
        return createToolBox(platformType);
    }



}
