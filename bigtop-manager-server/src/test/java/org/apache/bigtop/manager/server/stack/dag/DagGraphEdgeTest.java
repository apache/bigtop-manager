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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DagGraphEdgeTest {

    @Test
    public void testNormalCase() {
        DagGraphEdge edge = new DagGraphEdge("NodeA", "NodeB");
        assertEquals("NodeA", edge.getStartNode());
        assertEquals("NodeB", edge.getEndNode());
        assertEquals("NodeA -> NodeB", edge.toString());
    }

    @Test
    public void testStartNodeEmptyString() {
        DagGraphEdge edge = new DagGraphEdge("", "NodeB");
        assertEquals("", edge.getStartNode());
        assertEquals("NodeB", edge.getEndNode());
        assertEquals(" -> NodeB", edge.toString());
    }

    @Test
    public void testEndNodeEmptyString() {
        DagGraphEdge edge = new DagGraphEdge("NodeA", "");
        assertEquals("NodeA", edge.getStartNode());
        assertEquals("", edge.getEndNode());
        assertEquals("NodeA -> ", edge.toString());
    }

    @Test
    public void testBothNodesEmptyString() {
        DagGraphEdge edge = new DagGraphEdge("", "");
        assertEquals("", edge.getStartNode());
        assertEquals("", edge.getEndNode());
        assertEquals(" -> ", edge.toString());
    }

    @Test
    public void testStartNodeNull() {
        DagGraphEdge edge = new DagGraphEdge(null, "NodeB");
        assertNull(edge.getStartNode());
        assertEquals("NodeB", edge.getEndNode());
        assertEquals("null -> NodeB", edge.toString());
    }

    @Test
    public void testEndNodeNull() {
        DagGraphEdge edge = new DagGraphEdge("NodeA", null);
        assertEquals("NodeA", edge.getStartNode());
        assertNull(edge.getEndNode());
        assertEquals("NodeA -> null", edge.toString());
    }

    @Test
    public void testBothNodesNull() {
        DagGraphEdge edge = new DagGraphEdge(null, null);
        assertNull(edge.getStartNode());
        assertNull(edge.getEndNode());
        assertEquals("null -> null", edge.toString());
    }

    @Test
    public void testStartNodeEqualsEndNode() {
        DagGraphEdge edge = new DagGraphEdge("NodeA", "NodeA");
        assertEquals("NodeA", edge.getStartNode());
        assertEquals("NodeA", edge.getEndNode());
        assertEquals("NodeA -> NodeA", edge.toString());
    }

    @Test
    public void testStartNodeWithSpecialChars() {
        DagGraphEdge edge = new DagGraphEdge("Node@#1", "NodeB");
        assertEquals("Node@#1", edge.getStartNode());
        assertEquals("NodeB", edge.getEndNode());
        assertEquals("Node@#1 -> NodeB", edge.toString());
    }

    @Test
    public void testEndNodeWithSpecialChars() {
        DagGraphEdge edge = new DagGraphEdge("NodeA", "Node!@2");
        assertEquals("NodeA", edge.getStartNode());
        assertEquals("Node!@2", edge.getEndNode());
        assertEquals("NodeA -> Node!@2", edge.toString());
    }

    @Test
    public void testBothNodesWithSpecialChars() {
        DagGraphEdge edge = new DagGraphEdge("Node@#1", "Node!@2");
        assertEquals("Node@#1", edge.getStartNode());
        assertEquals("Node!@2", edge.getEndNode());
        assertEquals("Node@#1 -> Node!@2", edge.toString());
    }
}
