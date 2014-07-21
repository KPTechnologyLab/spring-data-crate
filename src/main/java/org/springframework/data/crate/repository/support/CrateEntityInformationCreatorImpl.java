/*
 * Copyright 2013-2014 the original author or authors.
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

import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * CrateEntityInformationCreatorImpl
 *
 * @author Rizwan Idrees
 */
public class CrateEntityInformationCreatorImpl implements CrateEntityInformationCreator {

	private final MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;

	public CrateEntityInformationCreatorImpl(
            MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		Assert.notNull(mappingContext);
		this.mappingContext = mappingContext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, ID extends Serializable> CrateEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {

		CratePersistentEntity<T> persistentEntity = (CratePersistentEntity<T>) mappingContext
				.getPersistentEntity(domainClass);

		Assert.notNull(persistentEntity, String.format("Unable to obtain mapping metadata for %s!", domainClass));

		return new MappingCrateEntityInformation<T, ID>(persistentEntity);
	}
}
