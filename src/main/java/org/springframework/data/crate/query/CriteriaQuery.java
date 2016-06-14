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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import static org.springframework.data.crate.query.Criteria.AND_OPERATOR;
import static org.springframework.data.crate.query.Criteria.OR_OPERATOR;

class CriteriaQuery extends PartialQuery {

    private Criteria criteria;

    CriteriaQuery() {
    }

    CriteriaQuery(Criteria criteria) {
        this.criteria = criteria;
    }

    <T extends CriteriaQuery> T addCriteria(Criteria criteria) {
        Preconditions.checkNotNull(criteria, "Cannot add null criteria.");
        if (this.criteria == null) {
            this.criteria = criteria;
        } else {
            this.criteria.and(criteria);
        }
        return (T) this;
    }

    Criteria getCriteria() {
        return this.criteria;
    }

    public StringBuilder buildSQLString() {
        StringBuilder builder = new StringBuilder();
        if (criteria == null)
            return null;

        String firstQuery = null;
        String conjunctionOperator = null;

        for (Criteria chainedCriteria : criteria.getCriteriaChain()) {
            String fragment = createCriteriaFragment(chainedCriteria);
            if (fragment != null) {
                if (firstQuery == null) {
                    firstQuery = fragment;
                    builder.append(firstQuery);
                    continue;
                }

                if (chainedCriteria.isOr()) {
                    if (AND_OPERATOR.equals(conjunctionOperator)) {
                        onOperatorSwitch(builder);
                    }
                    conjunctionOperator = OR_OPERATOR;
                    builder.append(conjunctionOperator);
                    builder.append(fragment);
                } else if (chainedCriteria.isAnd()) {
                    if (OR_OPERATOR.equals(conjunctionOperator)) {
                        onOperatorSwitch(builder);
                    }
                    conjunctionOperator = AND_OPERATOR;
                    builder.append(conjunctionOperator);
                    builder.append(fragment);
                }
            }
        }
        return builder;
    }

    private void onOperatorSwitch(StringBuilder builder) {
        builder.insert(0, "(");
        builder.append(")");
    }

    private String createCriteriaFragment(Criteria criteriaChain) {
        StringBuilder builder = new StringBuilder();
        if (criteriaChain.getQueryCriteriaEntries().isEmpty()) {
            return null;
        }

        Iterator<Criteria.CriteriaEntry> it = criteriaChain.getQueryCriteriaEntries().iterator();
        boolean singeEntryCriteria = (criteriaChain.getQueryCriteriaEntries().size() == 1);

        String fieldName = criteriaChain.getField().getName();
        Preconditions.checkNotNull(fieldName, "field cannot be null");

        if (singeEntryCriteria) {
            Criteria.CriteriaEntry entry = it.next();
            builder.append(processEntry(entry, fieldName));
        } else {
            while (it.hasNext()) {
                Criteria.CriteriaEntry entry = it.next();
                builder.append(processEntry(entry, fieldName));
            }
        }
        return builder.toString();
    }

    private String processEntry(Criteria.CriteriaEntry entry, String fieldName) {
        Object value = entry.getValue();
        Criteria.OperationKey key = entry.getKey();

        switch (key) {
            case EQUALS:
                return String.format("%s = %s", fieldName, print(value));
            case LIKE:
                return String.format("%s LIKE %s", fieldName, print(value));
            case STARTS_WITH:
                return String.format("%s LIKE '%s%%'", fieldName, value);
            case ENDS_WITH:
                return String.format("%s LIKE '%%%s'", fieldName, value);
            case LESS_EQUAL:
                return String.format("%s <= %s", fieldName, print(value));
            case GREATER_EQUAL:
                return String.format("%s >= %s", fieldName, print(value));
            case LESS:
                return String.format("%s < %s", fieldName, print(value));
            case GREATER:
                return String.format("%s > %s", fieldName, print(value));
            case IS_NULL:
                return String.format("%s IS NULL", fieldName);
        }
        throw new IllegalArgumentException(String.format("The %s key is not supported.", key));
    }

    private static String print(Object o) {
        if (o instanceof String) {
            return String.format("'%s'", o);
        } else if (o instanceof Number) {
            return String.valueOf(o);
        } else if (o instanceof HashSet) {
            throw new NotImplementedException();
        } else if (o instanceof Collection) {
            throw new NotImplementedException();
        } else if (o == null) {
            return "null";
        }
        return o.toString();
    }
}
