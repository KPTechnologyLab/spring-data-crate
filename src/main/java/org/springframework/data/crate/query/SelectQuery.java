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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

class SelectQuery implements MethodQuery {

    private final Set<String> roots;
    private final PartialQuery where;
    private final PartialQuery orderBy;
    private final PartialQuery groupBy;
    private final PartialQuery having;

    private SelectQuery(SelectQueryBuilder builder) {
        this.roots = builder.roots;
        this.where = builder.where;
        this.orderBy = builder.orderBy;
        this.groupBy = builder.groupBy;
        this.having = builder.having;
    }

    public static class SelectQueryBuilder {
        private Set<String> roots;
        private PartialQuery where;
        private PartialQuery orderBy;
        private PartialQuery groupBy;
        private PartialQuery having;

        public SelectQueryBuilder() {
        }

        public SelectQueryBuilder from(Set<String> roots) {
            Preconditions.checkNotNull(roots, "FROM must not be null!");
            Preconditions.checkArgument(!roots.isEmpty(), "FROM must not be empty!");
            this.roots = roots;
            return this;
        }

        public SelectQueryBuilder where(PartialQuery where) {
            this.where = where;
            return this;
        }

        public SelectQueryBuilder orderBy(PartialQuery orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public SelectQueryBuilder groupBy(PartialQuery groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        public SelectQueryBuilder having(PartialQuery having) {
            this.having = having;
            return this;
        }

        public SelectQuery build() {
            return new SelectQuery(this);
        }
    }

    @Override
    public StringBuilder buildSQLString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ");
        builder.append(StringUtils.join(roots, ", "));
        if (where != null) {
            builder.append(" WHERE ").append(this.where.buildSQLString());
        }
        if (orderBy != null) {
            builder.append(" ORDER BY ").append(this.orderBy.buildSQLString());
        }
        if (groupBy != null) {
            builder.append(" GROUP BY ").append(this.groupBy.buildSQLString());
        }
        if (having != null) {
            builder.append(" HAVING ").append(this.having.buildSQLString());
        }
        return builder;
    }

    @Override
    public String toString() {
        return this.buildSQLString().toString();
    }
}
