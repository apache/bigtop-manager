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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.solr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolrParamsTest {

    private final SolrParams kafkaParams = new SolrParams() {
        @Override
        public String serviceHome() {
            return "/confDir";
        }
    };

    @Test
    public void testConfDir() {
        assertEquals("/confDir/server/solr", kafkaParams.confDir());
    }

    @Test
    public void testGetServiceName() {
        assertEquals("solr", kafkaParams.getServiceName());
    }
}
