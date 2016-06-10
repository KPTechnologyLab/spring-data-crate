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

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CriteriaQueryTests {

    @Test
    public void testSQLRenderGreaterThanEqualCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").greaterThan(18));
        assertThat(criteria.buildSQLString().toString(), is("age > 18"));
        criteria = new WhereMethodQueryClause(new Criteria("age").greaterThanEqual(19));
        assertThat(criteria.buildSQLString().toString(), is("age >= 19"));
    }

    @Test
    public void testSQLRenderLessThanEqualCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").lessThan(18));
        assertThat(criteria.buildSQLString().toString(), is("age < 18"));
        criteria = new WhereMethodQueryClause(new Criteria("age").lessThanEqual(19));
        assertThat(criteria.buildSQLString().toString(), is("age <= 19"));
    }

    @Test
    public void testSQLRenderSimpleOrCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").is(18)
                .or(new Criteria("name").is("foo")));
        assertThat(criteria.buildSQLString().toString(), is("age = 18 OR name = 'foo'"));
    }

    @Test
    public void testSQLRenderSimpleAndCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").lessThan(18)
                .and(new Criteria("name").is("foo")));
        assertThat(criteria.buildSQLString().toString(), is("age < 18 AND name = 'foo'"));
    }

    @Test
    public void testSQLRenderChainedAndCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").lessThan(18)
                .and(new Criteria("name").is("bar"))
                .and(new Criteria("location").is("foo")));
        assertThat(criteria.buildSQLString().toString(), is("age < 18 AND name = 'bar' AND location = 'foo'"));
    }

    @Test
    public void testSQLRenderChainedAndOrCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").lessThan(18)
                .and(new Criteria("name").is("bar"))
                .or(new Criteria("location").is("foo")));
        assertThat(criteria.buildSQLString().toString(), is("(age < 18 AND name = 'bar') OR location = 'foo'"));
    }

    @Test
    public void testSQLRenderChainedMultipleAndWithOrCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("age").lessThan(18)
                .and(new Criteria("name").is("bar"))
                .and(new Criteria("lastname").is("bar"))
                .or(new Criteria("location").is("foo")));
        assertThat(criteria.buildSQLString().toString(),
                is("(age < 18 AND name = 'bar' AND lastname = 'bar') OR location = 'foo'"));
    }

    @Test
    public void testSQLRenderChainedOrAndCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("location").is("foo")
                .or(new Criteria("age").lessThan(18))
                .and(new Criteria("name").is("bar")));
        assertThat(criteria.buildSQLString().toString(), is("(location = 'foo' OR age < 18) AND name = 'bar'"));
    }

    @Test
    public void testSQLRenderChainedMultipleOrWithAndCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("location").is("foo")
                .or(new Criteria("age").lessThan(18))
                .or(new Criteria("height").lessThan(180))
                .and(new Criteria("name").is("bar")));
        assertThat(criteria.buildSQLString().toString(),
                is("(location = 'foo' OR age < 18 OR height < 180) AND name = 'bar'"));

        criteria = new WhereMethodQueryClause(new Criteria("location").is("foo")
                .or(new Criteria("age").lessThan(18))
                .or(new Criteria("height").lessThan(180))
                .and(new Criteria("name").is("bar"), new Criteria("weight").lessThan(80)));
        assertThat(criteria.buildSQLString().toString(),
                is("(location = 'foo' OR age < 18 OR height < 180) AND name = 'bar' AND weight < 80"));
    }

    @Test
    public void testSQLRenderWithLikeAndStartWithAndEndWithCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("location").like("foolocation")
                .and(new Criteria("name").startsWith("bar"))
                .and(new Criteria("lastname").endsWith("foo")));
        assertThat(criteria.buildSQLString().toString(),
                is("location LIKE 'foolocation' AND name LIKE 'bar%' AND lastname LIKE '%foo'"));
    }

    @Test
    public void testSQLRenderWithNullCriteria() {
        WhereMethodQueryClause criteria = new WhereMethodQueryClause(new Criteria("name").is(null));
        assertThat(criteria.buildSQLString().toString(), is("name IS NULL"));
    }
}
