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
package org.springframework.data.crate.core;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import io.crate.action.sql.SQLRequest;
import io.crate.client.CrateClient;

import java.util.ArrayList;

import org.elasticsearch.action.ActionListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.mapping.model.MappingException;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CrateTemplateTest {

	@Mock
	private CrateClient client;
	
	private CrateMappingContext mappingContext;
	private CrateConverter crateConverter;
	private CrateOperations crateOperations;
	
	@Before
	public void setup() {
		mappingContext = new CrateMappingContext();
		crateConverter = new MappingCrateConverter(mappingContext);
		crateOperations = new CrateTemplate(client, crateConverter);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotSaveCollectionType() {
		crateOperations.insert(asList("CRATE"));
		verify(client, never()).sql(any(SQLRequest.class), any(ActionListener.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotSaveCollectionSubType() {
		crateOperations.insert(new CollectionSubType());
		verify(client, never()).sql(any(SQLRequest.class), any(ActionListener.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotSaveIterableType() {
		crateOperations.insert(asList("CRATE").iterator());
		verify(client, never()).sql(any(SQLRequest.class), any(ActionListener.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotSaveArrayType() {
		crateOperations.insert(asList("CRATE").toArray());
		verify(client, never()).sql(any(SQLRequest.class), any(ActionListener.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=MappingException.class)
	public void shouldNotSaveWithNullId() {
		crateOperations.insert(new ClassWithSimpleId());
		verify(client, never()).sql(any(SQLRequest.class), any(ActionListener.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotFindByIdWhenIdIsNull() {
		crateOperations.findById(null, Object.class);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotRemoveWhenEntityHasNoId() {
		assertFalse(crateOperations.remove("id", CollectionSubType.class, "test"));
	}
	
	@Test
	public void shouldNotRemoveWhenObjectIsNull() {
		assertFalse(crateOperations.remove(null, Object.class, "test"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotFindByIdWhenEntityClassIsNull() {
		crateOperations.findById(1, null);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotUpdatedWhenEntityHasNoId() {
		crateOperations.update(new ClassWithNoId());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotUpdatedWhenIdIsNull() {
		crateOperations.update(new ClassWithSimpleId());
	}
	
	@Table(name="entity")
	static class ClassWithSimpleId {
		@Id
		String id;
	}
	
	@Table(name="entity")
	static class ClassWithNoId {
		String field;
	}
	
	@SuppressWarnings("serial")
	static class CollectionSubType extends ArrayList<String> {
	}
}