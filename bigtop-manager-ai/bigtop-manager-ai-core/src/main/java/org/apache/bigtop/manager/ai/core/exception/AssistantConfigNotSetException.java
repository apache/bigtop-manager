package org.apache.bigtop.manager.ai.core.exception;

/**
 * @Project: org.apache.bigtop.manager.ai.core.exception
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/11 14:35
 * @Description:
 */
public class AssistantConfigNotSetException extends RuntimeException{
    private String paramName;

    public AssistantConfigNotSetException(String paramName){
        super(paramName + " is a required parameter. You need to set.");
    }
}
