/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.data.crate.core.sql;

import org.springframework.data.crate.core.mapping.schema.Column;
import org.springframework.util.StringUtils;

import java.util.Iterator;

import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AlterTable extends AbstractStatement {

    private String tableName;
    private Column column;

    public AlterTable(String tableName, Column column) {

        hasText(tableName);
        notNull(column);
        this.tableName = tableName;
        this.column = column;
    }

    @Override
    public String createStatement() {

        if (!StringUtils.hasText(statement)) {

            StringBuilder builder = new StringBuilder(ALTER_TABLE).append(SPACE).
                    append(tableName).
                    append(SPACE).
                    append(ADD_COLUMN).
                    append(SPACE).
                    append(toSqlPath(column)).
                    append(SPACE).
                    append(createColumnDefinition(column));
            statement = builder.toString();
        }

        return statement;
    }

    private String createColumnDefinition(Column column) {

        StringBuilder builder = new StringBuilder();

        if (column.isArrayColumn()) {
            builder.append(column.getCrateType());
            builder.append(OPEN_BRACE);
            if (column.isPrimitiveElementType(column.getElementCrateType())) {
                builder.append(column.getElementCrateType());
            } else {
                createObjectColumnStatement(column, builder);
            }

            builder.append(CLOSE_BRACE);
        } else if (column.isObjectColumn()) {
            createObjectColumnStatement(column, builder);
        } else {
            builder.append(column.getCrateType());
        }

        if (column.isPrimaryKey()) {
            builder.append(SPACE)
                    .append(PRIMARY_KEY);
        }

        return builder.toString();
    }

    private void createObjectColumnStatement(Column column, StringBuilder builder) {

        builder.append(OBJECT);

        if (!column.getSubColumns().isEmpty()) {

            builder.append(SPACE)
                    .append(AS)
                    .append(SPACE)
                    .append(OPEN_BRACE);

            Iterator<Column> subColumns = column.getSubColumns().iterator();

            while (subColumns.hasNext()) {
                createColumnStatement(subColumns.next(), builder);
                if (subColumns.hasNext()) {
                    builder.append(COMMA)
                            .append(SPACE);
                }
            }

            builder.append(CLOSE_BRACE);
        }
    }

    private void createColumnStatement(Column column, StringBuilder builder) {

        // double quotes to preserve case in crate db
        builder.append(doubleQuote(column.getName()));
        builder.append(SPACE);

        if (column.isArrayColumn()) {
            builder.append(column.getCrateType());
            builder.append(OPEN_BRACE);
            if (column.isPrimitiveElementType(column.getElementCrateType())) {
                builder.append(column.getElementCrateType());
            } else {
                createObjectColumnStatement(column, builder);
            }

            builder.append(CLOSE_BRACE);
        } else if (column.isObjectColumn()) {
            createObjectColumnStatement(column, builder);
        } else {
            builder.append(column.getCrateType());
        }

        if (column.isPrimaryKey()) {
            builder.append(SPACE)
                    .append(PRIMARY_KEY);
        }
    }
}
