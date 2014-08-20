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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.sample.entities.Book;
import org.springframework.data.sample.entities.SampleEntity;

/**
 * 
 * @author Hasnain Javed
 *
 */
public class SimpleCratePersistentEntityTests {
	
	@Test
	public void shouldGetTableNameFromClass() {
		
		CrateMappingContext mappingContext = prepareMappingContext(SampleEntity.class);
		
		String tableName = mappingContext.getPersistentEntity(SampleEntity.class).getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(equalTo(SampleEntity.class.getSimpleName().toUpperCase())));
	}
	
	@Test
	public void shouldGetTableNameFromAnnotation() {
		
		CrateMappingContext mappingContext = prepareMappingContext(Book.class);
		
		String tableName = mappingContext.getPersistentEntity(Book.class).getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(equalTo(Book.class.getAnnotation(Table.class).name())));
	}
	
	private static CrateMappingContext prepareMappingContext(Class<?> type) {
		
		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();
		
		return mappingContext;
	}
}