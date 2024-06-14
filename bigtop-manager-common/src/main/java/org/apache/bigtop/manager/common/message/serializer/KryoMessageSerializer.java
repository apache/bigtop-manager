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
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;

import static org.apache.bigtop.manager.common.constants.Constants.KRYO_BUFFER_SIZE;

public class KryoMessageSerializer implements MessageSerializer {

    @Override
    public byte[] serialize(BaseMessage message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream, KRYO_BUFFER_SIZE);
        Kryo kryo = KryoPoolHolder.obtainKryo();
        kryo.writeClassAndObject(output, message);
        output.flush();
        output.close();
        KryoPoolHolder.freeKryo(kryo);

        return output.getBuffer();
    }
}
