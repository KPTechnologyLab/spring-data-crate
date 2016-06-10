/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.crate.query;

import com.google.common.base.Preconditions;

import java.util.*;

public class Criteria {

    static final String OR_OPERATOR = " OR ";
    static final String AND_OPERATOR = " AND ";

    private Field field;
    private boolean negating = false;

    private List<Criteria> criteriaChain = new ArrayList<>(1);
    private Set<CriteriaEntry> queryCriteria = new LinkedHashSet<>();

    public Criteria() {
    }

    public Criteria(String fieldName) {
        this(new SimpleField(fieldName));
    }

    public Criteria(Field field) {
        this.criteriaChain.add(this);
        this.field = field;
    }

    public Criteria(List<Criteria> criteriaChain, String fieldname) {
        this(criteriaChain, new SimpleField(fieldname));
    }

    public Criteria(List<Criteria> criteriaChain, Field field) {
        this.criteriaChain.addAll(criteriaChain);
        this.criteriaChain.add(this);
        this.field = field;
    }

    public static Criteria where(String field) {
        return where(new SimpleField(field));
    }

    public static Criteria where(Field field) {
        return new Criteria(field);
    }

    public Criteria and(Field field) {
        return new Criteria(this.criteriaChain, field);
    }

    public Criteria and(String fieldName) {
        return new Criteria(this.criteriaChain, fieldName);
    }

    public Criteria and(Criteria criteria) {
        this.criteriaChain.add(criteria);
        return this;
    }

    public Criteria and(Criteria... criterias) {
        this.criteriaChain.addAll(Arrays.asList(criterias));
        return this;
    }

    public Criteria or(Field field) {
        return new OrCriteria(this.criteriaChain, field);
    }

    public Criteria or(Criteria criteria) {
        Criteria orCriteria = new OrCriteria(this.criteriaChain, criteria.getField());
        orCriteria.queryCriteria.addAll(criteria.queryCriteria);
        return orCriteria;
    }

    public Criteria or(String fieldName) {
        return or(new SimpleField(fieldName));
    }

    public Criteria is(Object o) {
        if (o == null) {
            queryCriteria.add(new CriteriaEntry(OperationKey.IS_NULL, o));
            return this;
        }
        queryCriteria.add(new CriteriaEntry(OperationKey.EQUALS, o));
        return this;
    }

    public Criteria like(String s) {
        queryCriteria.add(new CriteriaEntry(OperationKey.LIKE, s));
        return this;
    }

    public Criteria expression(String s) {
        queryCriteria.add(new CriteriaEntry(OperationKey.EXPRESSION, s));
        return this;
    }

    public Criteria startsWith(String s) {
        queryCriteria.add(new CriteriaEntry(OperationKey.STARTS_WITH, s));
        return this;
    }

    public Criteria endsWith(String s) {
        queryCriteria.add(new CriteriaEntry(OperationKey.ENDS_WITH, s));
        return this;
    }

    public Criteria not() {
        this.negating = true;
        return this;
    }

    public Criteria lessThanEqual(Object upperBound) {
        Preconditions.checkNotNull(upperBound, "upper bound can't be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.LESS_EQUAL, upperBound));
        return this;
    }

    public Criteria lessThan(Object upperBound) {
        Preconditions.checkNotNull(upperBound, "upper bound can't be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.LESS, upperBound));
        return this;
    }

    public Criteria greaterThanEqual(Object lowerBound) {
        Preconditions.checkNotNull(lowerBound, "lower bound can't be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.GREATER_EQUAL, lowerBound));
        return this;
    }

    public Criteria greaterThan(Object lowerBound) {
        Preconditions.checkNotNull(lowerBound, "lower bound can't be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.GREATER, lowerBound));
        return this;
    }

    public Field getField() {
        return this.field;
    }

    public Set<CriteriaEntry> getQueryCriteriaEntries() {
        return Collections.unmodifiableSet(this.queryCriteria);
    }

    public String getConjunctionOperator() {
        return AND_OPERATOR;
    }

    public List<Criteria> getCriteriaChain() {
        return Collections.unmodifiableList(this.criteriaChain);
    }

    public boolean isNegating() {
        return this.negating;
    }

    public boolean isAnd() {
        return Objects.equals(AND_OPERATOR, getConjunctionOperator());
    }

    public boolean isOr() {
        return Objects.equals(OR_OPERATOR, getConjunctionOperator());
    }


    private static class OrCriteria extends Criteria {

        public OrCriteria() {
            super();
        }

        public OrCriteria(Field field) {
            super(field);
        }

        public OrCriteria(List<Criteria> criteriaChain, Field field) {
            super(criteriaChain, field);
        }

        public OrCriteria(List<Criteria> criteriaChain, String fieldName) {
            super(criteriaChain, fieldName);
        }

        public OrCriteria(String fieldName) {
            super(fieldName);
        }

        @Override
        public String getConjunctionOperator() {
            return OR_OPERATOR;
        }
    }

    enum OperationKey {
        EQUALS,
        LIKE,
        STARTS_WITH,
        ENDS_WITH,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        IS_NULL,
        EXPRESSION
    }

    static class CriteriaEntry {

        private OperationKey key;
        private Object value;

        CriteriaEntry(OperationKey key, Object value) {
            this.key = key;
            this.value = value;
        }

        public OperationKey getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    private static class SimpleField implements Field {

        private final String name;

        SimpleField(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    interface Field {
        String getName();
    }
}
