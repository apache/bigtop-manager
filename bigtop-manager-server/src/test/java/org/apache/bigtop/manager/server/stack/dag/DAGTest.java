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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DAGTest {

    private DAG<String, String, String> dag;

    @BeforeEach
    void setUp() {
        dag = new DAG<>();
    }

    // 测试节点是否存在
    @Test
    void testContainsNode() {
        assertFalse(dag.containsNode("A"));
        dag.addNodeIfAbsent("A", "Node A");
        assertTrue(dag.containsNode("A"));
    }

    // 测试获取节点数量
    @Test
    void testGetNodesCount() {
        assertEquals(0, dag.getNodesCount());
        dag.addNodeIfAbsent("A", "Node A");
        assertEquals(1, dag.getNodesCount());
    }

    // 测试获取边数量
    @Test
    void testGetEdgesCount() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertEquals(0, dag.getEdgesCount());
        dag.addEdge("A", "B");
        assertEquals(1, dag.getEdgesCount());
    }

    // 测试正常添加节点和边
    @Test
    void testAddEdgeHappyPath() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addNodeIfAbsent("C", "Node C");
        assertTrue(dag.addEdge("A", "B"));
        assertTrue(dag.addEdge("B", "C"));
        assertTrue(dag.addEdge("A", "C"));
        assertEquals(3, dag.getNodesCount());
        assertEquals(3, dag.getEdgesCount());
    }

    // 测试添加自环边
    @Test
    void testAddEdgeSelfLoop() {
        dag.addNodeIfAbsent("A", "Node A");
        assertFalse(dag.addEdge("A", "A"));
    }

    // 测试添加边形成环
    @Test
    void testAddEdgeCycle() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addNodeIfAbsent("C", "Node C");
        assertTrue(dag.addEdge("A", "B"));
        assertTrue(dag.addEdge("B", "C"));
        assertFalse(dag.addEdge("C", "A"));
    }

    // 测试边是否存在
    @Test
    void testContainsEdge() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertFalse(dag.containsEdge("A", "B"));
        dag.addEdge("A", "B");
        assertTrue(dag.containsEdge("A", "B"));
    }

    // 测试获取节点信息
    @Test
    void testGetNode() {
        assertNull(dag.getNode("A"));
        dag.addNodeIfAbsent("A", "Node A");
        assertEquals("Node A", dag.getNode("A"));
    }

    // 测试获取开始节点
    @Test
    void testGetBeginNode() {
        assertTrue(dag.getBeginNode().isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("A"), new LinkedList<>(dag.getBeginNode()));
    }

    // 测试获取结束节点
    @Test
    void testGetEndNode() {
        assertTrue(dag.getEndNode().isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("B"), new LinkedList<>(dag.getEndNode()));
    }

    // 测试获取前驱节点
    @Test
    void testGetPreviousNodes() {
        assertTrue(dag.getPreviousNodes("A").isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("A"), new LinkedList<>(dag.getPreviousNodes("B")));
    }

    // 测试获取后继节点
    @Test
    void testGetSubsequentNodes() {
        assertTrue(dag.getSubsequentNodes("A").isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("B"), new LinkedList<>(dag.getSubsequentNodes("A")));
    }

    // 测试获取节点入度
    @Test
    void testGetIndegree() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertEquals(0, dag.getIndegree("A"));
        dag.addEdge("A", "B");
        assertEquals(1, dag.getIndegree("B"));
    }

    // 测试拓扑排序
    @Test
    void testTopologicalSort() throws Exception {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addNodeIfAbsent("C", "Node C");
        List<String> expected = List.of("A", "B", "C");
        dag.addEdge("A", "B");
        dag.addEdge("B", "C");
        assertEquals(expected, dag.topologicalSort());
    }
}
