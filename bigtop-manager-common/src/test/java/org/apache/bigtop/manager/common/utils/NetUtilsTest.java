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
package org.apache.bigtop.manager.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class NetUtilsTest {

    @BeforeEach
    public void setup() {
        // Reset static variables
        try {
            // Use reflection to clear the static cached HOST_ADDRESS, HOST_NAME, and LOCAL_ADDRESS
            java.lang.reflect.Field hostAddressField = NetUtils.class.getDeclaredField("HOST_ADDRESS");
            hostAddressField.setAccessible(true);
            hostAddressField.set(null, null);

            java.lang.reflect.Field hostNameField = NetUtils.class.getDeclaredField("HOST_NAME");
            hostNameField.setAccessible(true);
            hostNameField.set(null, null);

            java.lang.reflect.Field localAddressField = NetUtils.class.getDeclaredField("LOCAL_ADDRESS");
            localAddressField.setAccessible(true);
            localAddressField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetHostSuccess() {
        InetAddress mockAddress = mock(InetAddress.class);
        when(mockAddress.getHostAddress()).thenReturn("192.168.0.1");

        // Mock InetAddress.getLocalHost() to return the mock address
        try (var mockedStatic = mockStatic(InetAddress.class)) {
            mockedStatic.when(InetAddress::getLocalHost).thenReturn(mockAddress);

            String host = NetUtils.getHost();
            assertEquals("192.168.0.1", host);
        }
    }

    @Test
    public void testGetHostFailure() {
        try (var mockedStatic = mockStatic(InetAddress.class)) {
            mockedStatic.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());
            // Should return default value 127.0.0.1
            String host = NetUtils.getHost();
            assertEquals("127.0.0.1", host);
        }
    }

    @Test
    public void testGetHostnameSuccess() {
        InetAddress mockAddress = mock(InetAddress.class);
        when(mockAddress.getHostName()).thenReturn("my-hostname");

        // Mock InetAddress.getLocalHost() to return the mock address
        try (var mockedStatic = mockStatic(InetAddress.class)) {
            mockedStatic.when(InetAddress::getLocalHost).thenReturn(mockAddress);

            String hostname = NetUtils.getHostname();
            assertEquals("my-hostname", hostname);
        }
    }

    @Test
    public void testGetHostnameFailure() {
        try (var mockedStatic = mockStatic(InetAddress.class)) {
            mockedStatic.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());
            // Should return default value localhost
            String hostname = NetUtils.getHostname();
            assertEquals("localhost", hostname);
        }
    }

    @Test
    public void testGetLocalAddressCaching() {
        InetAddress mockAddress = mock(InetAddress.class);
        when(mockAddress.getHostName()).thenReturn("my-hostname");

        try (var mockedStatic = mockStatic(InetAddress.class)) {
            mockedStatic.when(InetAddress::getLocalHost).thenReturn(mockAddress);

            // First call should return the address through mock
            String hostnameFirstCall = NetUtils.getHostname();
            assertEquals("my-hostname", hostnameFirstCall);

            // Second call should directly return the cached result
            String hostnameSecondCall = NetUtils.getHostname();
            assertEquals("my-hostname", hostnameSecondCall);
        }
    }
}
