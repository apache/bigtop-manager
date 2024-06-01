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
package org.apache.bigtop.manager.common.message.serializer;

import org.apache.bigtop.manager.common.message.entity.BaseMessage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class KryoMessageDeserializer implements MessageDeserializer {

    @Override
    public BaseMessage deserialize(byte[] bytes) {
        Input input = new Input(bytes);
        Kryo kryo = KryoPoolHolder.obtainKryo();
        BaseMessage baseMessage = (BaseMessage) kryo.readClassAndObject(input);
        input.close();
        KryoPoolHolder.freeKryo(kryo);

        return baseMessage;
    }
}
