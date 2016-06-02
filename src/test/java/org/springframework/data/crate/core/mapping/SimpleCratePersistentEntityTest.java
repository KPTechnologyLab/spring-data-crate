/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.data.crate.core.mapping;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.util.StringUtils.replace;

import java.util.Set;

import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.ColumnPolicy;
import static org.springframework.data.crate.core.mapping.schema.ColumnPolicy.*;
import org.springframework.data.crate.core.mapping.schema.TableParameters;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.sample.entities.Book;
import org.springframework.data.sample.entities.PropertiesContainer;
import org.springframework.data.sample.entities.SampleEntity;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class SimpleCratePersistentEntityTest {
	
	@Test(expected=MappingException.class)
	public void shouldNotCreatePersistentEntity() {
		prepareMappingContext(EntityWithInvalidColumnName.class);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotCreatePersistentEntityWithReservedIdName() {
		prepareMappingContext(EntityWithReservedIdName.class);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotCreatePersistentEntityWithReservedVersionName() {
		prepareMappingContext(EntityWithReservedVersionName.class);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotCreatePersistentEntityWithWrongVersionType() {
		prepareMappingContext(EntityWithWrongVersionType.class);
	}
	
	@Test
	public void shouldNotCreatePersistentEntityWithPrimitveVersionType() {
		prepareMappingContext(EntityWithPrimitveVersionType.class);
	}
	
	@Test
	public void shouldNotCreatePersistentEntityWithPrimitveWrapperVersionType() {
		prepareMappingContext(EntityWithPrimitveWrapperVersionType.class);
	}
	
	@Test
	public void shouldGetTableNameFromClass() {
		
		CrateMappingContext mappingContext = prepareMappingContext(SampleEntity.class);
		
		String tableName = mappingContext.getPersistentEntity(SampleEntity.class).getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(replace(SampleEntity.class.getName(), ".", "_")));
	}
	
	@Test
	public void shouldGetTableNameFromAnnotation() {
		
		CrateMappingContext mappingContext = prepareMappingContext(Book.class);
		
		String tableName = mappingContext.getPersistentEntity(Book.class).getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(Book.class.getAnnotation(Table.class).name()));
	}
	
	@Test
	public void shouldGetPropertyNames() {
		
		Set<String> propertyNames = prepareMappingContext(Book.class).
									getPersistentEntity(Book.class).
								    getPropertyNames();
		
		assertThat(propertyNames, is(notNullValue()));
		assertThat(propertyNames.isEmpty(), is(false));
		assertThat(propertyNames, hasItems("id", "title", "isbn"));
	}
	
	@Test
	public void shouldFilterPrimitiveFields() {
		
		Set<CratePersistentProperty> simpleProperties = prepareMappingContext(PropertiesContainer.class).
														getPersistentEntity(PropertiesContainer.class).
													    getPrimitiveProperties();
		assertThat(simpleProperties, is(notNullValue()));
		assertThat(simpleProperties.isEmpty(), is(false));
		assertThat(simpleProperties.size(), is(1));
		
		CratePersistentProperty simpleProperty = simpleProperties.iterator().next();
		assertThat(simpleProperty.isEntity(), is(false));
		assertThat(simpleProperty.isArray(), is(false));
		assertThat(simpleProperty.isCollectionLike(), is(false));
		assertThat(simpleProperty.isMap(), is(false));
		assertThat(simpleProperty.getActualType().getName(), is(String.class.getName()));
	}
	
	@Test
	public void shouldFilterEntityFields() {
		
		Set<CratePersistentProperty> compoisteProperties = prepareMappingContext(PropertiesContainer.class).
														   getPersistentEntity(PropertiesContainer.class).
														   getEntityProperties();
		assertThat(compoisteProperties, is(notNullValue()));
		assertThat(compoisteProperties.isEmpty(), is(false));
		assertThat(compoisteProperties.size(), is(1));
		
		CratePersistentProperty compositeProperty = compoisteProperties.iterator().next();
		assertThat(compositeProperty.isEntity(), is(true));
		assertThat(compositeProperty.isArray(), is(false));
		assertThat(compositeProperty.isCollectionLike(), is(false));
		assertThat(compositeProperty.isMap(), is(false));
		assertThat(compositeProperty.getActualType().getName(), is(Book.class.getName()));
	}
	
	@Test
	public void shouldFilterArrayFields() {
		
		Set<CratePersistentProperty> arrayProperties = prepareMappingContext(PropertiesContainer.class).
													   getPersistentEntity(PropertiesContainer.class).
													   getArrayProperties();
		assertThat(arrayProperties, is(notNullValue()));
		assertThat(arrayProperties.isEmpty(), is(false));
		assertThat(arrayProperties.size(), is(1));
		
		CratePersistentProperty arrayProperty = arrayProperties.iterator().next();
		assertThat(arrayProperty.isArray(), is(true));
		assertThat(arrayProperty.getRawType().isArray(), is(true));
	}
	
	@Test
	public void shouldFilterCollectionFields() {
		
		Set<CratePersistentProperty> collectionProperties = prepareMappingContext(PropertiesContainer.class).
														    getPersistentEntity(PropertiesContainer.class).
														    getCollectionProperties();
		assertThat(collectionProperties, is(notNullValue()));
		assertThat(collectionProperties.isEmpty(), is(false));
		assertThat(collectionProperties.size(), is(2));
		
		for(CratePersistentProperty collectionProperty : collectionProperties) {
			assertThat(collectionProperty.isArray(), is(false));
			assertThat(collectionProperty.isMap(), is(false));
			assertThat(collectionProperty.isCollectionLike(), is(true));
		}
	}
	
	@Test
	public void shouldFilterMapFields() {
		
		Set<CratePersistentProperty> mapProperties = prepareMappingContext(PropertiesContainer.class).
												     getPersistentEntity(PropertiesContainer.class).
												     getMapProperties();
		assertThat(mapProperties, is(notNullValue()));
		assertThat(mapProperties.isEmpty(), is(false));
		assertThat(mapProperties.size(), is(1));
		
		CratePersistentProperty mapProperty = mapProperties.iterator().next();
		assertThat(mapProperty.isArray(), is(false));
		assertThat(mapProperty.isCollectionLike(), is(false));
		assertThat(mapProperty.isMap(), is(true));
	}
	
	@Test
	public void shouldHaveDefaultTableParameters() {
		
		TableParameters parameters = prepareMappingContext(EntityWithDefaultTableParameters.class).
						             getPersistentEntity(EntityWithDefaultTableParameters.class).
						             getTableParameters();
		
		String defaultReplicas = EntityWithDefaultTableParameters.class.getAnnotation(Table.class).numberOfReplicas();
		int defaultRefreshInterval = EntityWithDefaultTableParameters.class.getAnnotation(Table.class).refreshInterval();
		ColumnPolicy defaultPolicy = EntityWithDefaultTableParameters.class.getAnnotation(Table.class).columnPolicy();
		
		assertThat(parameters, is(notNullValue()));
		assertThat(parameters.getNumberOfReplicas(), is(defaultReplicas));
		assertThat(parameters.getRefreshInterval(), is(defaultRefreshInterval));
		assertThat(parameters.getColumnPolicy(), is(defaultPolicy));
	}
	
	@Test
	public void shouldHaveCustomTableParameters() {
		
		TableParameters parameters = prepareMappingContext(EntityWithTableParameters.class).
						             getPersistentEntity(EntityWithTableParameters.class).
						             getTableParameters();
		
		String replicas = "2";
		int refreshInterval = 1500;
		ColumnPolicy policy = STRICT;
		
		assertThat(parameters, is(notNullValue()));
		assertThat(parameters.getNumberOfReplicas(), is(replicas));
		assertThat(parameters.getRefreshInterval(), is(refreshInterval));
		assertThat(parameters.getColumnPolicy(), is(policy));
	}
	
	private static CrateMappingContext prepareMappingContext(Class<?> type) {
		
		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();
		
		return mappingContext;
	}
	
	@Table
	static class EntityWithInvalidColumnName {
		String _stringColumn;
	}
	
	@Table
	static class EntityWithReservedIdName {
		@Id		
		long _id;
	}
	
	@Table
	static class EntityWithReservedVersionName {
		@Version		
		long _version;
	}
	
	@Table
	static class EntityWithWrongVersionType {
		@Version		
		String version;
	}
	
	@Table
	static class EntityWithPrimitveVersionType {
		@Version		
		long version;
	}
	
	@Table
	static class EntityWithPrimitveWrapperVersionType {
		@Version		
		Long version;
	}
	
	@Table
	static class EntityWithDefaultTableParameters {
		String string;
	}
	
	@Table(numberOfReplicas="2", refreshInterval=1500, columnPolicy=STRICT)
	static class EntityWithTableParameters {
		String string;
	}
}