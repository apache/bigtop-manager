package org.apache.bigtop.manager.ai.core.exception;

/**
 * @Project: org.apache.bigtop.manager.ai.core.exception
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:58
 * @Description:
 */
public class PlatformNotFoundException extends RuntimeException{
    public PlatformNotFoundException(String platform){
        super(platform + " platform not found. Please select one from PlatformType.");
    }



}
