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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTUtils {

    public static final String CLAIM_ID = "id";

    public static final String CLAIM_USERNAME = "username";

    public static final String CLAIM_TOKEN_VERSION = "token_version";

    protected static final String SIGN = "r0PGVyvjKOxUBwGt";

    // Token validity period (days)
    private static final int TOKEN_EXPIRATION_DAYS = 7;

    public static String generateToken(Long userId, String username, Integer tokenVersion) {
        Instant expireTime = Instant.now().plus(TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS);

        return JWT.create()
                .withClaim(CLAIM_ID, userId)
                .withClaim(CLAIM_USERNAME, username)
                .withClaim(CLAIM_TOKEN_VERSION, tokenVersion)
                .withExpiresAt(expireTime)
                .sign(Algorithm.HMAC256(SIGN));
    }

    public static DecodedJWT resolveToken(String token) {
        return JWT.require(Algorithm.HMAC256(SIGN)).build().verify(token);
    }
}
