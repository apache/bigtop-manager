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
import com.google.common.cache.CacheStats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Multi-cache utility class supporting different expiration times and enhanced generic support
 */
public class CacheUtils {

    /**
     * Default cache configuration
     */
    private static final int DEFAULT_EXPIRE_TIME = 5;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    private static final int DEFAULT_MAXIMUM_SIZE = 10000;

    /**
     * Multiple cache instances mapped by cache name
     */
    private static final ConcurrentHashMap<String, Cache<String, Object>> CACHES = new ConcurrentHashMap<>();

    /**
     * Create or get cache with specific configuration
     * Note: If a cache with the same name already exists, it will be returned regardless
     * of the provided configuration parameters.
     *
     * @param cacheName   cache name/identifier
     * @param expireTime  expire time
     * @param timeUnit    time unit
     * @param maximumSize maximum cache size
     * @return cache instance
     */
    public static Cache<String, Object> getOrCreateCache(
            String cacheName, int expireTime, TimeUnit timeUnit, int maximumSize) {
        if (StringUtils.isBlank(cacheName)) {
            throw new IllegalArgumentException("Cache name cannot be blank");
        }

        return CACHES.computeIfAbsent(cacheName, name -> CacheBuilder.newBuilder()
                .expireAfterWrite(expireTime, timeUnit)
                .maximumSize(maximumSize)
                .recordStats()
                .build());
    }

    /**
     * Create or get cache with custom expire time (in minutes) and default size
     *
     * @param cacheName     cache name/identifier
     * @param expireMinutes expire time in minutes
     * @return cache instance
     */
    public static Cache<String, Object> getOrCreateCache(String cacheName, int expireMinutes) {
        return getOrCreateCache(cacheName, expireMinutes, DEFAULT_TIME_UNIT, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Create or get cache with custom expire time and time unit, using default size
     *
     * @param cacheName  cache name/identifier
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    public static Cache<String, Object> getOrCreateCache(String cacheName, int expireTime, TimeUnit timeUnit) {
        return getOrCreateCache(cacheName, expireTime, timeUnit, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Get cache with default configuration
     *
     * @param cacheName cache name/identifier
     * @return cache instance
     */
    public static Cache<String, Object> getOrCreateCache(String cacheName) {
        return getOrCreateCache(cacheName, DEFAULT_EXPIRE_TIME, DEFAULT_TIME_UNIT, DEFAULT_MAXIMUM_SIZE);
    }

    /**
     * Store cache value
     *
     * @param cacheName cache name/identifier
     * @param key       cache key
     * @param value     cache value
     */
    public static void setCache(String cacheName, String key, Object value) {
        if (StringUtils.isAnyBlank(cacheName, key) || value == null) {
            return;
        }
        getOrCreateCache(cacheName).put(key, value);
    }

    /**
     * Store cache value with specific cache configuration
     *
     * @param cacheName  cache name/identifier
     * @param key        cache key
     * @param value      cache value
     * @param expireTime expire time
     */
    public static void setCache(String cacheName, String key, Object value, int expireTime) {
        if (StringUtils.isAnyBlank(cacheName, key) || value == null) {
            return;
        }
        getOrCreateCache(cacheName, expireTime).put(key, value);
    }

    /**
     * Store cache value with specific cache configuration
     *
     * @param cacheName  cache name/identifier
     * @param key        cache key
     * @param value      cache value
     * @param expireTime expire time
     * @param timeUnit   time unit
     */
    public static void setCache(String cacheName, String key, Object value, int expireTime, TimeUnit timeUnit) {
        if (StringUtils.isAnyBlank(cacheName, key) || value == null) {
            return;
        }
        getOrCreateCache(cacheName, expireTime, timeUnit).put(key, value);
    }

    /**
     * Store cache value with specific cache configuration
     *
     * @param cacheName   cache name/identifier
     * @param key         cache key
     * @param value       cache value
     * @param expireTime  expire time
     * @param timeUnit    time unit
     * @param maximumSize maximum cache size
     */
    public static void setCache(
            String cacheName, String key, Object value, int expireTime, TimeUnit timeUnit, int maximumSize) {
        if (StringUtils.isAnyBlank(cacheName, key) || value == null) {
            return;
        }
        getOrCreateCache(cacheName, expireTime, timeUnit, maximumSize).put(key, value);
    }

    /**
     * Batch set cache values
     *
     * @param cacheName cache name/identifier
     * @param values    map of key-value pairs to cache
     */
    public static void setCacheBatch(String cacheName, Map<String, Object> values) {
        if (StringUtils.isBlank(cacheName) || values == null || values.isEmpty()) {
            return;
        }
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        cache.putAll(values);
    }

    /**
     * Get cache value with type safety
     *
     * @param cacheName cache name/identifier
     * @param key       cache key
     * @param clazz     expected class type
     * @param <T>       generic type
     * @return cache value of specified type (returns null if not found, expired or type mismatch)
     */
    public static <T> T getCache(String cacheName, String key, Class<T> clazz) {
        if (StringUtils.isAnyBlank(cacheName, key) || clazz == null) {
            return null;
        }

        Object value = getCache(cacheName, key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    /**
     * Generic version of getCache method
     *
     * @param cacheName cache name/identifier
     * @param key       cache key
     * @param <T>       generic type
     * @return cache value (returns null if not found or expired)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCache(String cacheName, String key) {
        if (StringUtils.isAnyBlank(cacheName, key)) {
            return null;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        return cache != null ? (T) cache.getIfPresent(key) : null;
    }

    /**
     * Check if cache with given name exists
     *
     * @param cacheName cache name/identifier
     * @return true if cache exists, false otherwise
     */
    public static boolean exists(String cacheName) {
        return StringUtils.isNotBlank(cacheName) && CACHES.containsKey(cacheName);
    }

    /**
     * Remove specified cache entry
     *
     * @param cacheName cache name/identifier
     * @param key       cache key
     */
    public static void removeCache(String cacheName, String key) {
        if (StringUtils.isAnyBlank(cacheName, key)) {
            return;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * Clear all entries in specific cache
     *
     * @param cacheName cache name/identifier
     */
    public static void clearCache(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
            cache.cleanUp();
        }
    }

    /**
     * Clear all caches
     */
    public static void clearAll() {
        CACHES.values().forEach(cache -> {
            cache.invalidateAll();
            cache.cleanUp();
        });
        CACHES.clear();
    }

    /**
     * Get cache size for specific cache
     *
     * @param cacheName cache name/identifier
     * @return cache size
     */
    public static long size(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return 0;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        return cache != null ? cache.size() : 0;
    }

    /**
     * Get total size of all caches
     *
     * @return total cache size
     */
    public static long totalSize() {
        return CACHES.values().stream().mapToLong(Cache::size).sum();
    }

    /**
     * Get cache statistics
     *
     * @param cacheName cache name/identifier
     * @return cache statistics
     */
    public static CacheStats getStats(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return null;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        return cache != null ? cache.stats() : null;
    }

    /**
     * Force cleanup of expired entries in all caches
     */
    public static void cleanupAll() {
        CACHES.values().forEach(Cache::cleanUp);
    }

    /**
     * Force cleanup of expired entries in specific cache
     *
     * @param cacheName cache name/identifier
     */
    public static void cleanup(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return;
        }
        Cache<String, Object> cache = CACHES.get(cacheName);
        if (cache != null) {
            cache.cleanUp();
        }
    }
}
