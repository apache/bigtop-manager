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

import org.apache.bigtop.manager.server.model.query.PageQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.data.domain.Sort;

public class PageUtils {

    private static final String PAGE_NUM = "pageNum";

    private static final String PAGE_SIZE = "pageSize";

    private static final String ORDER_BY = "orderBy";

    private static final String SORT = "sort";

    private static final Integer DEFAULT_PAGE_NUM = 1;

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private static final String DEFAULT_ORDER_BY = "id";

    private static final String SORT_ASC = "asc";

    private static final String SORT_DESC = "desc";

    public static PageQuery getPageQuery() {
        PageQuery query = new PageQuery();
        query.setPageNum(NumberUtils.toInt(ServletUtils.getParameter(PAGE_NUM), DEFAULT_PAGE_NUM) - 1);
        query.setPageSize(NumberUtils.toInt(ServletUtils.getParameter(PAGE_SIZE), DEFAULT_PAGE_SIZE));

        String orderBy = StringUtils.defaultIfBlank(ServletUtils.getParameter(ORDER_BY), DEFAULT_ORDER_BY);
        String sort = StringUtils.defaultIfBlank(ServletUtils.getParameter(SORT), SORT_ASC);
        if (SORT_DESC.equals(sort)) {
            query.setSort(Sort.by(orderBy).descending());
        } else {
            query.setSort(Sort.by(orderBy).ascending());
        }

        return query;
    }
}
