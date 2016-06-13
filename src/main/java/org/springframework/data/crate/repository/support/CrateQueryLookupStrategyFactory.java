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

import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.query.AnnotatedCrateRepositoryQuery;
import org.springframework.data.crate.query.CrateQueryMethod;
import org.springframework.data.crate.query.PartTreeCrateRepositoryQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.Locale;

public final class CrateQueryLookupStrategyFactory {

    private static class CrateAbstractLookupStrategy implements QueryLookupStrategy {

        protected final CrateOperations operations;
        protected final EvaluationContextProvider context;

        CrateAbstractLookupStrategy(CrateOperations operations, EvaluationContextProvider context) {
            this.operations = operations;
            this.context = context;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method,
                                            RepositoryMetadata repositoryMetadata,
                                            ProjectionFactory projectionFactory,
                                            NamedQueries namedQueries) {
            throw new UnsupportedOperationException("Spring Data Crate does not support this query lookup strategy.");
        }

    }

    static class DeclaredQueryLookupStrategy extends CrateAbstractLookupStrategy {

        DeclaredQueryLookupStrategy(CrateOperations operations, EvaluationContextProvider context) {
            super(operations, context);
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata repositoryMetadata,
                                            ProjectionFactory projectionFactory, NamedQueries namedQueries) {
            CrateQueryMethod queryMethod = new CrateQueryMethod(method, repositoryMetadata, projectionFactory);
            return new AnnotatedCrateRepositoryQuery(queryMethod, operations);
        }
    }

    static class CreateQueryLookupStrategy extends CrateAbstractLookupStrategy {

        CreateQueryLookupStrategy(CrateOperations operations, EvaluationContextProvider context) {
            super(operations, context);
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata repositoryMetadata,
                                            ProjectionFactory projectionFactory, NamedQueries namedQueries) {
            CrateQueryMethod queryMethod = new CrateQueryMethod(method, repositoryMetadata, projectionFactory);
            return new PartTreeCrateRepositoryQuery(queryMethod, operations);
        }
    }

    static class CreateIfNotFoundQueryLookupStrategy extends CrateAbstractLookupStrategy {

        CreateIfNotFoundQueryLookupStrategy(CrateOperations operations, EvaluationContextProvider context) {
            super(operations, context);
        }

        @Override
        public RepositoryQuery resolveQuery(Method method,
                                            RepositoryMetadata repositoryMetadata,
                                            ProjectionFactory projectionFactory,
                                            NamedQueries namedQueries) {
            try {
                return (new CreateQueryLookupStrategy(operations, context))
                        .resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries);
            } catch (Exception e) {
                return (new DeclaredQueryLookupStrategy(operations, context))
                        .resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries);
            }
        }
    }

    public static QueryLookupStrategy create(CrateOperations operations,
                                             QueryLookupStrategy.Key key,
                                             EvaluationContextProvider context) {
        if (key == null) {
            return new CreateQueryLookupStrategy(operations, context);
        }

        switch (key) {
            case CREATE:
                return new CreateQueryLookupStrategy(operations, context);
            case USE_DECLARED_QUERY:
                return new DeclaredQueryLookupStrategy(operations, context);
            case CREATE_IF_NOT_FOUND:
                return new CreateIfNotFoundQueryLookupStrategy(operations, context);
            default:
                throw new UnsupportedOperationException(String.format(Locale.ENGLISH,
                        "Spring Data Crate does not support %s query lookup strategy.", key));
        }
    }
}
