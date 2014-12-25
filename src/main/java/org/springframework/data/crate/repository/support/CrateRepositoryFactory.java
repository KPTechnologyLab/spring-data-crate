/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.crate.repository.support;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

/**
 * Factory to create {@link org.springframework.data.crate.repository.CrateRepository}
 *
 * @author Rizwan Idrees
 */
public class CrateRepositoryFactory extends RepositoryFactorySupport {

	private final CrateOperations crateOperations;
	private final CrateEntityInformationCreator entityInformationCreator;

	public CrateRepositoryFactory(CrateOperations crateOperations) {
		Assert.notNull(crateOperations);
		this.crateOperations = crateOperations;
		this.entityInformationCreator = new CrateEntityInformationCreatorImpl(crateOperations.getConverter().getMappingContext());
	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	@Override
	public <T, ID extends Serializable> CrateEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return entityInformationCreator.getEntityInformation(domainClass);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Object getTargetRepository(RepositoryMetadata metadata) {
        return new SimpleCrateRepository(getEntityInformation(metadata.getDomainType()),crateOperations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		return SimpleCrateRepository.class;
	}

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        return new CrateQueryLookupStrategy();
    }

    private class CrateQueryLookupStrategy implements QueryLookupStrategy {
		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
            //TODO: implement this method
            return null;
		}
	}
}
