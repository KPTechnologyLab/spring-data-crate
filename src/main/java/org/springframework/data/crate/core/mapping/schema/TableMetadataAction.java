/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.crate.core.mapping.schema;

import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;
import org.springframework.data.crate.core.CrateAction;
import org.springframework.data.crate.core.CrateActionResponseHandler;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;

import java.util.List;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
class TableMetadataAction implements CrateAction, CrateActionResponseHandler<TableMetadata> {

    private static final String SELECT_TEMPLATE = "select table_name, number_of_replicas from information_schema.tables where table_name = '%s'";

    private String tableName;
    private TableParameters parameters;
    private List<ColumnMetadata> columns;

    /*
     * TODO: Remove constructor once Crate returns refresh interval
     * and column policy from information_schema.tables
     */
    public TableMetadataAction(CratePersistentEntity<?> entity, List<ColumnMetadata> columns) {
        this(entity.getTableName(), columns);
        this.parameters = entity.getTableParameters();
    }

    public TableMetadataAction(String tableName, List<ColumnMetadata> columns) {

        hasText(tableName);
        notEmpty(columns);

        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public String getSQLStatement() {
        return format(SELECT_TEMPLATE, tableName.toLowerCase());
    }

    /*
     * TODO: get refresh_interval and column_policy from response instead of entity
     * once Crate returns the required information from information_schema.tables
     */
    @Override
    public TableMetadata handle(SQLResponse response) {

        Object[][] rows = response.rows();

        String tableName = valueOf(rows[0][0]);
        String numberOfReplicas = valueOf(rows[0][1]);
        int refreshInterval = parameters.getRefreshInterval();
        ColumnPolicy policy = parameters.getColumnPolicy();

        return new TableMetadata(tableName, columns, new TableParameters(numberOfReplicas, refreshInterval, policy));
    }

    @Override
    public SQLRequest getSQLRequest() {
        return new SQLRequest(getSQLStatement());
    }
}
