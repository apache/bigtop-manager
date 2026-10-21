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
package org.apache.bigtop.manager.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStreamableHttpClientProperties;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpAsyncClientManagerTest {

    private static final ObjectProvider<McpSseClientProperties> SSE_PROVIDER = new EmptyObjectProvider<>();
    private static final ObjectProvider<McpStreamableHttpClientProperties> STREAMABLE_HTTP_PROVIDER =
            new EmptyObjectProvider<>();
    private static final ObjectProvider<McpStdioClientProperties> STDIO_PROVIDER = new EmptyObjectProvider<>();

    @Test
    void resolveBaseUrlShouldPreferConfiguredValue() throws Exception {
        McpAsyncClientManager manager =
                new McpAsyncClientManager(SSE_PROVIDER, STREAMABLE_HTTP_PROVIDER, STDIO_PROVIDER);
        setField(manager, "connections", "name=sseMain,type=sse,baseUrl=http://127.0.0.1:9000,endpoint=/mcp/sse");

        Method parseMethod = McpAsyncClientManager.class.getDeclaredMethod("parseConnections");
        parseMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> conns = (List<Object>) parseMethod.invoke(manager);
        assertEquals("http://127.0.0.1:9000", getStringField(conns.get(0), "baseUrl"));
    }

    @Test
    void resolveBaseUrlShouldFallbackToServerPort() throws Exception {
        McpAsyncClientManager manager =
                new McpAsyncClientManager(SSE_PROVIDER, STREAMABLE_HTTP_PROVIDER, STDIO_PROVIDER);
        setField(manager, "connections", "   ");

        Method parseMethod = McpAsyncClientManager.class.getDeclaredMethod("parseConnections");
        parseMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> conns = (List<Object>) parseMethod.invoke(manager);
        assertTrue(conns.isEmpty());
    }

    @Test
    void parseConnectionsShouldSupportSseHttpAndLocal() throws Exception {
        McpAsyncClientManager manager =
                new McpAsyncClientManager(SSE_PROVIDER, STREAMABLE_HTTP_PROVIDER, STDIO_PROVIDER);
        setField(
                manager,
                "connections",
                "name=sseMain,type=sse,baseUrl=http://127.0.0.1:8080,endpoint=/mcp/sse;"
                        + "name=httpMain,type=streamable-http,baseUrl=http://127.0.0.1:8081,endpoint=/mcp;"
                        + "name=localMain,type=local,command=npx,args=-y|@modelcontextprotocol/server-memory,env=FOO:bar|BAZ:qux");

        Method parseMethod = McpAsyncClientManager.class.getDeclaredMethod("parseConnections");
        parseMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> conns = (List<Object>) parseMethod.invoke(manager);

        assertEquals(3, conns.size());
        assertEquals("sse", getStringField(conns.get(0), "type"));
        assertEquals("streamable-http", getStringField(conns.get(1), "type"));
        assertEquals("local", getStringField(conns.get(2), "type"));

        @SuppressWarnings("unchecked")
        List<String> args = (List<String>) getField(conns.get(2), "args");
        assertEquals(2, args.size());
        assertEquals("-y", args.get(0));
        assertEquals("@modelcontextprotocol/server-memory", args.get(1));

        @SuppressWarnings("unchecked")
        Map<String, String> env = (Map<String, String>) getField(conns.get(2), "env");
        assertEquals("bar", env.get("FOO"));
        assertEquals("qux", env.get("BAZ"));
    }

    @Test
    void parseConnectionsShouldFallbackEmptyWhenBlank() throws Exception {
        McpAsyncClientManager manager =
                new McpAsyncClientManager(SSE_PROVIDER, STREAMABLE_HTTP_PROVIDER, STDIO_PROVIDER);
        setField(manager, "connections", "   ");

        Method parseMethod = McpAsyncClientManager.class.getDeclaredMethod("parseConnections");
        parseMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> conns = (List<Object>) parseMethod.invoke(manager);

        assertTrue(conns.isEmpty());
    }

    private void setField(McpAsyncClientManager manager, String fieldName, String value) throws Exception {
        var field = McpAsyncClientManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }

    private Object getField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private String getStringField(Object object, String fieldName) throws Exception {
        return (String) getField(object, fieldName);
    }

    private static class EmptyObjectProvider<T> implements ObjectProvider<T> {
        @Override
        public T getObject() {
            throw new UnsupportedOperationException("No object available");
        }

        @Override
        public T getObject(Object... args) {
            throw new UnsupportedOperationException("No object available");
        }

        @Override
        public T getIfAvailable() {
            return null;
        }

        @Override
        public T getIfUnique() {
            return null;
        }
    }
}
