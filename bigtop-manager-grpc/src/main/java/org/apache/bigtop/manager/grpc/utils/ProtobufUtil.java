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
package org.apache.bigtop.manager.grpc.utils;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public class ProtobufUtil {

    @SuppressWarnings("unchecked")
    public static <T extends MessageOrBuilder> T fromJson(String json, Class<T> clazz) {
        try {
            Message.Builder builder =
                    (Message.Builder) clazz.getMethod("newBuilder").invoke(null);
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return (T) builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Message> String toJson(T message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
