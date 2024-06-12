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
package org.apache.bigtop.manager.stack.common.utils;

import org.apache.bigtop.manager.stack.common.exception.StackException;
import org.apache.bigtop.manager.stack.common.log.TaskLogWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Map;

@Slf4j
public class XmlUtils {

    private static final DocumentBuilder db;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            db = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * writeXml to file
     *
     * @param fileName  fileName
     * @param configMap Map<String, Object>
     */
    public static void writeXml(String fileName, Map<String, Object> configMap) {
        try {
            Document document = db.newDocument();
            // don't display standalone="no"
            document.setXmlStandalone(true);

            Element configuration = document.createElement("configuration");

            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                TaskLogWriter.info(entry.getKey() + " " + entry.getValue());
                Element property = document.createElement("property");

                Element name = document.createElement("name");
                Element value = document.createElement("value");
                name.setTextContent(entry.getKey());
                value.setTextContent(String.valueOf(entry.getValue()));

                property.appendChild(name);
                property.appendChild(value);
                configuration.appendChild(property);
            }

            document.appendChild(configuration);

            TransformerFactory tff = TransformerFactory.newInstance();
            tff.setAttribute("indent-number", "2");
            Transformer tf = tff.newTransformer();

            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");

            tf.transform(new DOMSource(document), new StreamResult(new File(fileName)));
            TaskLogWriter.info("writeXml " + fileName + " success");
        } catch (TransformerException e) {
            TaskLogWriter.error("writeXml error: " + e.getLocationAsString());
            throw new StackException(e);
        }
    }
}
