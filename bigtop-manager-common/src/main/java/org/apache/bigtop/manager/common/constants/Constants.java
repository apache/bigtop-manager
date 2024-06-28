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
package org.apache.bigtop.manager.common.constants;

import java.io.File;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Construct Constants");
    }

    /**
     * stack cache dir
     */
    public static final String STACK_CACHE_DIR =
            new File(Constants.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .getPath())
                            .getParent() + "/../cache";

    /**
     * host key for all hosts
     */
    public static final String ALL_HOST_KEY = "all";

    /**
     * permission 755
     */
    public static final String PERMISSION_755 = "rwxr-xr-x";
    /**
     * permission 644
     */
    public static final String PERMISSION_644 = "rw-r--r--";
    /**
     * permission 777
     */
    public static final String PERMISSION_777 = "rwwrwxrwx";

    /**
     * root user
     */
    public static final String ROOT_USER = "root";
}
