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

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * {@link TableParameters} holds a table's parameters.
 *
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class TableParameters {

    private final String numberOfReplicas;
    private final int refreshInterval;
    private final ColumnPolicy columnPolicy;

    public TableParameters(String numberOfReplicas, int refreshInterval, ColumnPolicy columnPolicy) {

        hasText(numberOfReplicas);
        notNull(columnPolicy);

        this.numberOfReplicas = numberOfReplicas;
        this.refreshInterval = refreshInterval;
        this.columnPolicy = columnPolicy;
    }

    public String getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public ColumnPolicy getColumnPolicy() {
        return columnPolicy;
    }
}
