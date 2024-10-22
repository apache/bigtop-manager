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

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.password.PasswordIdentityProvider;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.util.security.SecurityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class RemoteSSHUtils {

    public static ShellResult executeCommand(String host, Integer port, String user, String command) throws Exception {
        try (SshClient client = getSshClient()) {
            return run(client, host, port, user, command);
        }
    }

    public static ShellResult executeCommand(String host, Integer port, String user, String password, String command)
            throws Exception {
        try (SshClient client = getSshClient(password)) {
            return run(client, host, port, user, command);
        }
    }

    public static ShellResult executeCommand(
            String host,
            Integer port,
            String user,
            String keyFilename,
            String keyString,
            String keyPassword,
            String command)
            throws Exception {
        String keyPath = ProjectPathUtils.getKeyStorePath() + File.separator + keyFilename;
        try (SshClient client = getSshClient(keyPath, keyString, keyPassword)) {
            return run(client, host, port, user, command);
        }
    }

    public static ShellResult run(SshClient client, String host, Integer port, String user, String command)
            throws Exception {
        client.start();
        try (ClientSession session =
                client.connect(user, host, port).verify(10, TimeUnit.SECONDS).getSession()) {
            session.auth().verify(10, TimeUnit.SECONDS);
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
                    ClientChannel channel = session.createExecChannel(command)) {
                channel.setOut(responseStream);
                channel.setErr(errStream);
                channel.open().verify(10, TimeUnit.SECONDS);
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);
                String res = responseStream.toString();
                String err = errStream.toString();
                return new ShellResult(channel.getExitStatus(), res, err);
            }
        } finally {
            client.stop();
        }
    }

    public static SshClient getSshClient() {
        return SshClient.setUpDefaultClient();
    }

    public static SshClient getSshClient(String password) {
        SshClient client = SshClient.setUpDefaultClient();
        client.setPasswordIdentityProvider(PasswordIdentityProvider.wrapPasswords(password));
        return client;
    }

    public static SshClient getSshClient(String keyPath, String keyString, String keyPassword) throws Exception {
        SshClient client = SshClient.setUpDefaultClient();
        KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
        FilePasswordProvider passwordProvider = null;
        if (StringUtils.isNotBlank(keyPassword)) {
            passwordProvider = FilePasswordProvider.of(keyPassword);
        }

        Collection<KeyPair> keys = null;
        if (StringUtils.isNotBlank(keyPath)) {
            keys = loader.loadKeyPairs(null, new File(keyPath).toPath(), passwordProvider);
        } else {
            InputStream keyStream = new ByteArrayInputStream(keyString.getBytes(StandardCharsets.UTF_8));
            keys = loader.loadKeyPairs(null, () -> "keyStringResource", passwordProvider, keyStream);
        }
        client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(keys));
        return client;
    }
}
