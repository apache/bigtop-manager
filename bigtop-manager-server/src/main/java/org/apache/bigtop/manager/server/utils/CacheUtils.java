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
     * Default cache configuration
     */
    private static final int DEFAULT_EXPIRE_TIME = 5;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    private static final int DEFAULT_MAXIMUM_SIZE = 10000;

    /**
     * Cache instance
     */
    private static Cache<String, Object> CACHE;

    static {
        // Static init with default configuration
        initCache(DEFAULT_EXPIRE_TIME, DEFAULT_TIME_UNIT, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Initialize cache with custom configuration
     *
     * @param expireTime  expire time
     * @param timeUnit    time unit
     * @param maximumSize maximum cache size
     */
    public static synchronized void initCache(int expireTime, TimeUnit timeUnit, int maximumSize) {
        CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(expireTime, timeUnit)
                .maximumSize(maximumSize)
                .build();
    }

    /**
     * Initialize cache with custom expire time (in minutes) and default size
     *
     * @param expireMinutes expire time in minutes
     */
    public static void initCache(int expireMinutes) {
        initCache(expireMinutes, TimeUnit.MINUTES, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Initialize cache with custom expire time and time unit, using default size
     *
     * @param expireTime expire time
     * @param timeUnit   time unit
     */
    public static void initCache(int expireTime, TimeUnit timeUnit) {
        initCache(expireTime, timeUnit, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Store cache
     *
     * @param key   cache key
     * @param value cache value
     */
    public static void setCache(String key, Object value) {
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
    public static Object getCache(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return CACHE.getIfPresent(key);
    }

    /**
     * Get cache with type safety
     *
     * @param key   cache key
     * @param clazz expected class type
     * @param <T>   generic type
     * @return cache value of specified type (returns null if not found, expired or type mismatch)
     */
    public static <T> T getCache(String key, Class<T> clazz) {
        if (StringUtils.isBlank(key) || clazz == null) {
            return null;
        }

        Object value = getCache(key);
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
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
