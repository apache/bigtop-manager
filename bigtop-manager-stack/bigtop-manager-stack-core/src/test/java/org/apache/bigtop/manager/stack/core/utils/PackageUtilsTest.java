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
package org.apache.bigtop.manager.stack.core.utils;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.repo.YumPackageManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PackageUtilsTest {

    @Mock
    private YumPackageManager yumPackageManager;

    private MockedStatic<PackageUtils> packageUtilsMockedStatic;

    @BeforeEach
    public void setUp() {
        packageUtilsMockedStatic = mockStatic(PackageUtils.class);
        packageUtilsMockedStatic.when(PackageUtils::getPackageManager).thenReturn(yumPackageManager);
    }

    @AfterEach
    public void tearDown() {
        packageUtilsMockedStatic.close();
    }

    @Test
    public void testInstall() {
        packageUtilsMockedStatic.when(() -> PackageUtils.install(any())).thenCallRealMethod();

        ShellResult expectedResult = new ShellResult();
        expectedResult.setExitCode(0);
        expectedResult.setErrMsg("Installation successful");

        when(yumPackageManager.installPackage(any())).thenReturn(expectedResult);

        ShellResult result = PackageUtils.install(Arrays.asList("package1", "package2"));

        assertEquals(0, result.getExitCode());
        assertEquals("Installation successful", result.getErrMsg());

        verify(yumPackageManager, times(1)).installPackage(any());
    }

    @Test
    public void testInstallWithEmptyPackageList() {
        packageUtilsMockedStatic.when(() -> PackageUtils.install(any())).thenCallRealMethod();

        ShellResult result = PackageUtils.install(Collections.emptyList());

        assertEquals(-1, result.getExitCode());
        assertEquals("packageList is empty", result.getErrMsg());

        verify(yumPackageManager, never()).installPackage(any());
    }
}
