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

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JWTUtilsTest {

    @Test
    public void testGenerateTokenNormal() {
        Long id = 1L;
        String username = "testUser";
        Integer tokenVersion = 1;
        String token = JWTUtils.generateToken(id, username, tokenVersion);
        assertNotNull(token);

        DecodedJWT decodedJWT = JWTUtils.resolveToken(token);
        assertEquals(id, decodedJWT.getClaim(JWTUtils.CLAIM_ID).asLong());
        assertEquals(username, decodedJWT.getClaim(JWTUtils.CLAIM_USERNAME).asString());
        assertEquals(
                tokenVersion, decodedJWT.getClaim(JWTUtils.CLAIM_TOKEN_VERSION).asInt());
    }

    @Test
    public void testResolveTokenExpired() {
        Long id = 2L;
        String username = "expiredUser";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date date = calendar.getTime();

        String token = JWT.create()
                .withClaim(JWTUtils.CLAIM_ID, id)
                .withClaim(JWTUtils.CLAIM_USERNAME, username)
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(JWTUtils.SIGN));

        assertThrows(JWTVerificationException.class, () -> JWTUtils.resolveToken(token));
    }

    @Test
    public void testResolveTokenIllegal() {
        String illegalToken =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        assertThrows(JWTVerificationException.class, () -> JWTUtils.resolveToken(illegalToken));
    }

    @Test
    public void testResolveTokenWrongFormat() {
        String wrongFormatToken = "wrong_format_token";
        assertThrows(JWTDecodeException.class, () -> JWTUtils.resolveToken(wrongFormatToken));
    }

    @Test
    public void testGenerateTokenUsernameEmpty() {
        String token = JWTUtils.generateToken(1L, "", 1);
        assertNotNull(token);

        DecodedJWT decodedJWT = JWTUtils.resolveToken(token);
        assertEquals("", decodedJWT.getClaim(JWTUtils.CLAIM_USERNAME).asString());
    }
}
