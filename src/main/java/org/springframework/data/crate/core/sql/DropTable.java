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

import static org.springframework.util.Assert.hasText;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class DropTable extends AbstractStatement {

    private final String tableName;

    public DropTable(String tableName) {
        super();
        hasText(tableName);
        this.tableName = tableName;
    }

    @Override
    public String createStatement() {
        return DROP_TABLE.concat(SPACE).concat(tableName);
    }
}
