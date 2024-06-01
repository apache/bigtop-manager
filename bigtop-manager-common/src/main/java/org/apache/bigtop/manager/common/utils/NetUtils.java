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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtils {

    private static InetAddress LOCAL_ADDRESS = null;
    private static volatile String HOST_ADDRESS;
    private static volatile String HOST_NAME;

    public static String getHost() {
        if (HOST_ADDRESS != null) {
            return HOST_ADDRESS;
        }
        try {
            InetAddress address = getLocalAddress();
            HOST_ADDRESS = address.getHostAddress();
            return HOST_ADDRESS;
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public static String getHostname() {
        if (HOST_NAME != null) {
            return HOST_NAME;
        }
        try {
            InetAddress address = getLocalAddress();
            HOST_NAME = address.getHostName();
            return HOST_NAME;
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private static InetAddress getLocalAddress() throws UnknownHostException {
        if (null != LOCAL_ADDRESS) {
            return LOCAL_ADDRESS;
        }
        LOCAL_ADDRESS = InetAddress.getLocalHost();
        return LOCAL_ADDRESS;
    }

}
