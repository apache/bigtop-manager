package org.apache.bigtop.manager.ai.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Project: org.apache.bigtop.manager.ai.common.enums
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:40
 * @Description:
 */
public enum PlatformType {


    OPENAI("openai");






    private final String value;
    PlatformType(String value){
        this.value = value;
    }

    public static List<String> getPlatforms(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }

    public static PlatformType getPlatformType(String value){
        if(Objects.isNull(value) || value.isEmpty()){
            return null;
        }
        for(PlatformType platformType: PlatformType.values()){
            if(platformType.value.equals(value)){
                return platformType;
            }
        }
        return null;
    }

    public String getValue(){
        return this.value;
    }

}
