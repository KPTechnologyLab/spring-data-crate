/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */
package org.springframework.data.crate.query;

import io.crate.action.sql.SQLResponse;
import io.crate.types.DataType;
import org.springframework.data.crate.core.CrateActionResponseHandler;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.CrateDocumentConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.crate.core.mapping.CrateMappingContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.Assert.notNull;

public class SimpleQueryCrateHandler<T> implements CrateActionResponseHandler<List<T>> {

    private final Class<T> type;
    private final CrateConverter converter;

    public SimpleQueryCrateHandler(Class<T> type) {
        notNull(type);
        this.type = type;
        this.converter = new MappingCrateConverter(new CrateMappingContext());
    }

    @Override
    public List<T> handle(SQLResponse response) {
        if (response.hasRowCount()) {
            String[] columns = response.cols();
            DataType<?>[] types = response.columnTypes();
            Object[][] payload = response.rows();

            Long rows = response.rowCount();
            List<T> entities = new ArrayList<>(rows.intValue());

            for (Object[] row : payload) {
                CrateDocument source = new CrateDocumentConverter(columns, types, row).toDocument();

                T entity = null;
                if (!source.isEmpty()) {
                    entity = converter.read(type, source);
                }

                if (entity != null) {
                    entities.add(entity);
                }
            }
            return entities;
        } else {
            return emptyList();
        }
    }
}
