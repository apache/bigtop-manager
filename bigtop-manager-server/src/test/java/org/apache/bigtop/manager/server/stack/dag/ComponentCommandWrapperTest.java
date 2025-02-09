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
package org.apache.bigtop.manager.server.stack.dag;

import org.apache.bigtop.manager.common.enums.Command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComponentCommandWrapperTest {

    @Test
    public void testEqualsAndHashcode() {
        ComponentCommandWrapper wrapper1 = new ComponentCommandWrapper("HDFS", Command.START);
        ComponentCommandWrapper wrapper2 = new ComponentCommandWrapper("HDFS", Command.START);
        ComponentCommandWrapper wrapper3 = new ComponentCommandWrapper("YARN", Command.START);
        ComponentCommandWrapper wrapper4 = new ComponentCommandWrapper("HDFS", Command.STOP);

        // Test equals method
        assertEquals(wrapper1, wrapper2); // Same component name and command
        assertNotEquals(wrapper1, wrapper3); // Different component name
        assertNotEquals(wrapper1, wrapper4); // Same component name but different command

        // Test hashCode method
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode()); // Same component name and command
        assertNotEquals(wrapper1.hashCode(), wrapper3.hashCode()); // Different component name
        assertNotEquals(wrapper1.hashCode(), wrapper4.hashCode()); // Same component name but different command
    }

    @Test
    public void testComponentNameNull() {
        ComponentCommandWrapper wrapper1 = new ComponentCommandWrapper(null, Command.START);
        ComponentCommandWrapper wrapper2 = new ComponentCommandWrapper(null, Command.START);
        ComponentCommandWrapper wrapper3 = new ComponentCommandWrapper(null, Command.STOP);

        assertEquals(wrapper1, wrapper2); // Same command and both component names are null
        assertNotEquals(wrapper1, wrapper3); // Same component name but different command

        assertEquals(wrapper1.hashCode(), wrapper2.hashCode()); // Same command and both component names are null
        assertNotEquals(wrapper1.hashCode(), wrapper3.hashCode()); // Same component name but different command
    }

    @Test
    public void testCommandNull() {
        ComponentCommandWrapper wrapper1 = new ComponentCommandWrapper("HDFS", null);
        ComponentCommandWrapper wrapper2 = new ComponentCommandWrapper("HDFS", null);
        ComponentCommandWrapper wrapper3 = new ComponentCommandWrapper("YARN", null);

        assertEquals(wrapper1, wrapper2); // Same component name and both commands are null
        assertNotEquals(wrapper1, wrapper3); // Different component name

        assertEquals(wrapper1.hashCode(), wrapper2.hashCode()); // Same component name and both commands are null
        assertNotEquals(wrapper1.hashCode(), wrapper3.hashCode()); // Different component name
    }

    @Test
    public void testComponentNameAndCommandNull() {
        ComponentCommandWrapper wrapper1 = new ComponentCommandWrapper(null, null);
        ComponentCommandWrapper wrapper2 = new ComponentCommandWrapper(null, null);

        assertEquals(wrapper1, wrapper2); // Both component name and command are null

        assertEquals(wrapper1.hashCode(), wrapper2.hashCode()); // Both component name and command are null
    }

    @Test
    public void testToString() {
        ComponentCommandWrapper wrapper1 = new ComponentCommandWrapper("HDFS", Command.START);
        ComponentCommandWrapper wrapper2 = new ComponentCommandWrapper("YARN", Command.STOP);

        assertEquals("HDFS-START", wrapper1.toString()); // Normal component name and command
        assertEquals("YARN-STOP", wrapper2.toString()); // Normal component name and command
    }

    @Test
    public void testToStringWithComponentNameNull() {
        ComponentCommandWrapper wrapper = new ComponentCommandWrapper(null, Command.START);

        assertEquals("null-START", wrapper.toString()); // Component name is null
    }

    @Test
    public void testToStringWithCommandNull() {
        ComponentCommandWrapper wrapper = new ComponentCommandWrapper("HDFS", null);

        assertThrows(NullPointerException.class, wrapper::toString);
    }
}
