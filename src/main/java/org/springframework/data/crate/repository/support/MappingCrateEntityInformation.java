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

import static java.lang.String.format;
import static org.springframework.util.Assert.notNull;

import java.io.Serializable;

import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

/**
 * Crate specific implementation of
 * {@link org.springframework.data.repository.core.support.AbstractEntityInformation}
 *
 * @param <T>
 * @param <ID>
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
public class MappingCrateEntityInformation<T, ID extends Serializable> extends AbstractEntityInformation<T, ID> 
												 implements CrateEntityInformation<T, ID> {

	private static final String ID_MSG = "Unable to identify 'id' property in class '%s'."
										 .concat("Make sure the 'id' property is annotated with @Id or named as 'id'");
	
	private final CratePersistentEntity<T> entityMetadata;
	private Class<?> idClass;
	
	public MappingCrateEntityInformation(CratePersistentEntity<T> entity) {
		
		super(entity.getType());
		this.entityMetadata = entity;
		this.idClass = entity.getIdProperty().getType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID getId(T entity) {
		
		if(entityMetadata.hasIdProperty()) {
			return (ID) entityMetadata.getPropertyAccessor(entity)
					  				  .getProperty(entityMetadata.getIdProperty());
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ID> getIdType() {
		return (Class<ID>) idClass;
	}
	
	@Override
	public String getTableName() {
		return entityMetadata.getTableName();
	}

	@Override
	public String getIdAttribute() {
		
		notNull(entityMetadata.getIdProperty(), format(ID_MSG, entityMetadata.getType().getSimpleName()));
		return entityMetadata.getIdProperty().getFieldName();
	}

	@Override
	public Long getVersion(T entity) {
		
		if(entityMetadata.hasVersionProperty()) {
			return (Long) entityMetadata.getPropertyAccessor(entity)
										.getProperty(entityMetadata.getVersionProperty());
		}
		
		return null;
	}
}