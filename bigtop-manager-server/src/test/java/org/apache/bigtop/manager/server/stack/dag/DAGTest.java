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

    // Test if node exists
    @Test
    void testContainsNode() {
        assertFalse(dag.containsNode("A"));
        dag.addNodeIfAbsent("A", "Node A");
        assertTrue(dag.containsNode("A"));
    }

    // Test getting the number of nodes
    @Test
    void testGetNodesCount() {
        assertEquals(0, dag.getNodesCount());
        dag.addNodeIfAbsent("A", "Node A");
        assertEquals(1, dag.getNodesCount());
    }

    // Test getting the number of edges
    @Test
    void testGetEdgesCount() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertEquals(0, dag.getEdgesCount());
        dag.addEdge("A", "B");
        assertEquals(1, dag.getEdgesCount());
    }

    // Test adding nodes and edges normally
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

    // Test adding a self-loop edge
    @Test
    void testAddEdgeSelfLoop() {
        dag.addNodeIfAbsent("A", "Node A");
        assertFalse(dag.addEdge("A", "A"));
    }

    // Test adding an edge that would form a cycle
    @Test
    void testAddEdgeCycle() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addNodeIfAbsent("C", "Node C");
        assertTrue(dag.addEdge("A", "B"));
        assertTrue(dag.addEdge("B", "C"));
        assertFalse(dag.addEdge("C", "A"));
    }

    // Test if edge exists
    @Test
    void testContainsEdge() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertFalse(dag.containsEdge("A", "B"));
        dag.addEdge("A", "B");
        assertTrue(dag.containsEdge("A", "B"));
    }

    // Test getting node information
    @Test
    void testGetNode() {
        assertNull(dag.getNode("A"));
        dag.addNodeIfAbsent("A", "Node A");
        assertEquals("Node A", dag.getNode("A"));
    }

    // Test getting beginning nodes
    @Test
    void testGetBeginNode() {
        assertTrue(dag.getBeginNode().isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("A"), new LinkedList<>(dag.getBeginNode()));
    }

    // Test getting end nodes
    @Test
    void testGetEndNode() {
        assertTrue(dag.getEndNode().isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("B"), new LinkedList<>(dag.getEndNode()));
    }

    // Test getting previous nodes
    @Test
    void testGetPreviousNodes() {
        assertTrue(dag.getPreviousNodes("A").isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("A"), new LinkedList<>(dag.getPreviousNodes("B")));
    }

    // Test getting subsequent nodes
    @Test
    void testGetSubsequentNodes() {
        assertTrue(dag.getSubsequentNodes("A").isEmpty());
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        dag.addEdge("A", "B");
        assertEquals(Collections.singletonList("B"), new LinkedList<>(dag.getSubsequentNodes("A")));
    }

    // Test getting the indegree of a node
    @Test
    void testGetIndegree() {
        dag.addNodeIfAbsent("A", "Node A");
        dag.addNodeIfAbsent("B", "Node B");
        assertEquals(0, dag.getIndegree("A"));
        dag.addEdge("A", "B");
        assertEquals(1, dag.getIndegree("B"));
    }

    // Test topological sorting
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
