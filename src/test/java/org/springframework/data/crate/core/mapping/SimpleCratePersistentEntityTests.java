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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.sample.entities.Book;
import org.springframework.data.sample.entities.SampleEntity;
import org.springframework.data.util.TypeInformation;

/**
 * 
 * @author Hasnain Javed
 *
 */
public class SimpleCratePersistentEntityTests {
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldGetTableNameFromClass() {
		
		TypeInformation<SampleEntity> mock = Mockito.mock(TypeInformation.class);
		
		when(mock.getType()).thenReturn(SampleEntity.class);
		
		CratePersistentEntity<SampleEntity> entity = new SimpleCratePersistentEntity<SampleEntity>(mock);
		
		String tableName = entity.getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(equalTo(SampleEntity.class.getSimpleName().toUpperCase())));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldGetTableNameFromAnnotation() {
		
		TypeInformation<Book> mock = Mockito.mock(TypeInformation.class);
		
		when(mock.getType()).thenReturn(Book.class);
		
		CratePersistentEntity<Book> entity = new SimpleCratePersistentEntity<Book>(mock);
		
		String tableName = entity.getTableName();
		
		assertThat(tableName, is(notNullValue()));
		assertThat(tableName, is(equalTo(Book.class.getAnnotation(Table.class).name())));
	}
}