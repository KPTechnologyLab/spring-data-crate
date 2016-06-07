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

package org.springframework.data.crate.repository.support;

import io.crate.client.CrateClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.CrateTemplate;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.repository.query.DefaultEvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class QueryLookUpStrategyTest {

    private static CrateRepositoryFactory factory;

    @BeforeClass
    public static void beforeClass() {
        CrateMappingContext mappingContext = new CrateMappingContext();
        CrateOperations crateOperations = new CrateTemplate(
                mock(CrateClient.class),
                new MappingCrateConverter(mappingContext)
        );
        factory = new CrateRepositoryFactory(crateOperations);
    }

    @Test
    public void testDeclaredQueryLookupStrategy() {
        QueryLookupStrategy lookupStrategy = factory.getQueryLookupStrategy(
                QueryLookupStrategy.Key.USE_DECLARED_QUERY,
                DefaultEvaluationContextProvider.INSTANCE
        );
        assertThat(lookupStrategy, instanceOf(CrateQueryLookupStrategyFactory.DeclaredQueryLookupStrategy.class));
    }

    @Test
    public void testCreateIfNotFoundQueryLookupStrategy() {
        QueryLookupStrategy lookupStrategy = factory.getQueryLookupStrategy(
                QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND,
                DefaultEvaluationContextProvider.INSTANCE
        );
        assertThat(lookupStrategy, instanceOf(CrateQueryLookupStrategyFactory.CreateIfNotFoundQueryLookupStrategy.class));
    }

    @Test
    public void testCreateQueryLookupStrategy() {
        QueryLookupStrategy lookupStrategy = factory.getQueryLookupStrategy(
                QueryLookupStrategy.Key.CREATE,
                DefaultEvaluationContextProvider.INSTANCE
        );
        assertThat(lookupStrategy, instanceOf(CrateQueryLookupStrategyFactory.CreateQueryLookupStrategy.class));
    }

}
