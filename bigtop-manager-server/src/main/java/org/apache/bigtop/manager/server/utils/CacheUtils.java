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
package org.apache.bigtop.manager.server.utils;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Cache utility class
 */
public class CacheUtils {

    /**
     * Create cache, expires in 5 minutes, maximum capacity 10000
     */
    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    /**
     * Store cache
     *
     * @param key   cache key
     * @param value cache value
     */
    public static void setCache(String key, String value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        CACHE.put(key, value);
    }

    /**
     * Get cache
     *
     * @param key cache key
     * @return cache value (returns null if not found or expired)
     */
    public static String getCache(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return CACHE.getIfPresent(key);
    }

    /**
     * Remove specified cache
     *
     * @param key cache key
     */
    public static void removeCache(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        CACHE.invalidate(key);
    }

    /**
     * Clear all caches
     */
    public static void clearAll() {
        CACHE.invalidateAll();
        CACHE.cleanUp();
    }

    /**
     * Get cache size
     */
    public static long size() {
        return CACHE.size();
    }
}
