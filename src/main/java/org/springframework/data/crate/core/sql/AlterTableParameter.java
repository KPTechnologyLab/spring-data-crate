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
package org.springframework.data.crate.core.sql;

import org.springframework.data.crate.core.mapping.schema.AlterTableDefinition.AlterTableParameterDefinition;
import org.springframework.util.StringUtils;

import static java.lang.String.valueOf;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AlterTableParameter extends AbstractStatement {

    private String tableName;
    private String parameterName;
    private Object parameterValue;

    public AlterTableParameter(String tableName, AlterTableParameterDefinition def) {

        hasText(tableName);
        notNull(def);

        this.tableName = tableName;
        this.parameterName = def.getParameterName();
        this.parameterValue = def.getParameterValue();
    }

    @Override
    public String createStatement() {

        if (!StringUtils.hasText(statement)) {

            StringBuilder builder = new StringBuilder(ALTER_TABLE).append(SPACE).
                    append(tableName).
                    append(SPACE).
                    append(SET).
                    append(SPACE).
                    append(OPEN_BRACE).
                    append(parameterName).
                    append(SPACE).
                    append("=").
                    append(SPACE).
                    append(singleQuote(valueOf(parameterValue))).
                    append(CLOSE_BRACE);

            statement = builder.toString();
        }

        return statement;
    }
}
