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
package org.apache.bigtop.manager.server.model.vo;

import org.apache.bigtop.manager.server.exception.ServerException;

import org.mapstruct.factory.Mappers;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageVO<T> {

    private Long total;

    private List<T> content;

    public static <T> PageVO<T> of(List<T> content, Long total) {
        PageVO<T> res = new PageVO<>();
        res.setContent(content);
        res.setTotal(total);
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <T, S> PageVO<T> of(PageInfo<S> page) {
        List<T> content = new ArrayList<>();
        if (page.hasContent()) {
            try {
                Class<S> clz = (Class<S>) page.getList().get(0).getClass();
                String className = "org.apache.bigtop.manager.server.model.converter."
                        + clz.getSimpleName().replace("PO", "") + "Converter";
                Class<?> mapper = Class.forName(className);
                Object o = Mappers.getMapper(mapper);
                Method method = o.getClass().getDeclaredMethod("fromPO2VO", List.class);
                content = (List<T>) method.invoke(o, page.getList());
            } catch (Exception e) {
                throw new ServerException(e);
            }
        }

        PageVO<T> res = new PageVO<>();
        res.setContent(content);
        res.setTotal(page.getTotal());
        return res;
    }
}
