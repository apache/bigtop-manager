/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.stack.common.utils.template;

import org.apache.bigtop.manager.stack.common.exception.StackException;

import freemarker.core.UndefinedOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.TimeZone;

@Slf4j
public class BaseTemplate {

    public static final Configuration CONFIGURATION;

    static {
        CONFIGURATION = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        CONFIGURATION.setClassForTemplateLoading(BaseTemplate.class, "/templates");
        CONFIGURATION.setDefaultEncoding("UTF-8");
        CONFIGURATION.setOutputFormat(UndefinedOutputFormat.INSTANCE);
        CONFIGURATION.setLogTemplateExceptions(false);
        CONFIGURATION.setWrapUncheckedExceptions(true);
        CONFIGURATION.setFallbackOnNullLoopVariable(false);
        CONFIGURATION.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
        /* fix number_format display error */
        CONFIGURATION.setNumberFormat("0.##");
        // Set the behavior when the attribute does not exist, IGNORE_HANDLER will ignore the error
        CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
    }

    public static void writeTemplate(String path, Object dataModel, String type) {
        /* Get the template (uses cache internally) */
        try {
            Template template = CONFIGURATION.getTemplate(type + ".ftl");
            writeTemplate(path, dataModel, template);
        } catch (IOException e) {
            log.error("Failed to writeTemplate", e);
        }
    }

    public static void writeCustomTemplate(String path, Object dataModel, String sourceStr) {
        /* Get the template (uses cache internally) */
        try {
            Template template = new Template("tmpTemplate", sourceStr, CONFIGURATION);
            writeTemplate(path, dataModel, template);
        } catch (IOException e) {
            log.error("Failed to writeTemplate", e);
        }
    }

    public static void writeTemplate(String path, Object dataModel, Template template) {
        FileWriter fileWriter = null;
        try {
            /* Merge data-model with template */
            fileWriter = new FileWriter(path, false);
            template.process(dataModel, fileWriter);
            fileWriter.flush();
        } catch (TemplateException | IOException e) {
            log.error("Failed to writeTemplate", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static String writeTemplateAsString(Object dataModel, String type) {
        /* Get the template (uses cache internally) */
        Template template = null;
        try {
            template = CONFIGURATION.getTemplate(type + ".ftl");
            return writeTemplateAsString(dataModel, template);
        } catch (IOException e) {
            log.error("Failed to writeTemplate", e);
            throw new StackException(e);
        }
    }

    public static String writeCustomTemplateAsString(Object dataModel, String sourceStr) {
        /* Get the template (uses cache internally) */
        try {
            Template template = new Template("tmpTemplate", sourceStr, CONFIGURATION);
            return writeTemplateAsString(dataModel, template);
        } catch (IOException e) {
            log.error("Failed to writeTemplate", e);
            throw new StackException(e);
        }
    }

    public static String writeTemplateAsString(Object dataModel, Template template) {
        StringWriter stringWriter = null;
        try {
            /* Merge data-model with template */
            stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            stringWriter.flush();
        } catch (TemplateException | IOException e) {
            log.error("Failed to writeTemplate", e);
        } finally {
            try {
                if (stringWriter != null) {
                    stringWriter.close();
                }
            } catch (IOException ignore) {
            }
        }
        return stringWriter.toString();
    }
}
