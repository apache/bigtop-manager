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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.BaseMessage;
import org.apache.bigtop.manager.common.message.entity.BaseRequestMessage;
import org.apache.bigtop.manager.common.message.entity.BaseResponseMessage;
import org.apache.bigtop.manager.common.message.entity.HeartbeatMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandLogMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandMessageType;
import org.apache.bigtop.manager.common.message.entity.command.CommandRequestMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandResponseMessage;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ComponentInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.CustomCommandInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.HostCheckType;
import org.apache.bigtop.manager.common.message.entity.pojo.HostInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.OSSpecificInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ScriptInfo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class KryoPoolHolder {

    private static final Pool<Kryo> KRYO_POOL = new Pool<>(true, false, 16) {

        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setCopyReferences(true);

            // message types
            kryo.register(BaseMessage.class);
            kryo.register(BaseRequestMessage.class);
            kryo.register(BaseResponseMessage.class);
            kryo.register(HeartbeatMessage.class);
            kryo.register(CommandResponseMessage.class);
            kryo.register(CommandRequestMessage.class);
            kryo.register(CommandLogMessage.class);

            // message pojo
            kryo.register(HostInfo.class);
            kryo.register(OSSpecificInfo.class);
            kryo.register(ClusterInfo.class);
            kryo.register(RepoInfo.class);
            kryo.register(HostCheckType.class);
            kryo.register(HostCheckType[].class);
            kryo.register(CommandMessageType.class);
            kryo.register(Command.class);
            kryo.register(ScriptInfo.class);
            kryo.register(ComponentInfo.class);
            kryo.register(CustomCommandInfo.class);

            // java classes
            kryo.register(BigDecimal.class);
            kryo.register(Timestamp.class);
            kryo.register(ArrayList.class);
            kryo.register(Integer.class);
            kryo.register(String.class);
            kryo.register(HashMap.class);
            kryo.register(LinkedHashMap.class);
            kryo.register(HashSet.class);

            return kryo;
        }
    };

    public static Kryo obtainKryo() {
        return KRYO_POOL.obtain();
    }

    public static void freeKryo(Kryo kryo) {
        KRYO_POOL.free(kryo);
    }
}
