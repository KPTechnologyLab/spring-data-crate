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

package org.springframework.data.crate.core.mapping.schema;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.crate.core.mapping.CrateDataType.INTEGER;
import static org.springframework.data.crate.core.mapping.CrateDataType.STRING;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;
import io.crate.action.sql.SQLResponse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.crate.NoSuchTableException;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.CrateSQLAction;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CratePersistentEntitySchemaManagerTest {

	@Mock
	private CrateOperations crateOperations;
	
	private CrateMappingContext mappingContext;
	private CrateConverter crateConverter;
	
	@Before
	public void setup() {
		mappingContext = new CrateMappingContext();
		crateConverter = new MappingCrateConverter(mappingContext);
		when(crateOperations.getConverter()).thenReturn(crateConverter);
	}
	
	@Test(expected=DataAccessException.class)
	public void shouldStopSchemaExportOnError() throws Exception {
		
		initializeMappingContext(Person.class);
		
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new InvalidDataAccessResourceUsageException("Error!!"));
		
		try {
			CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
			manager.setExportOption(CREATE);
			manager.afterPropertiesSet();
			fail("should have stopped exporting schema");
		}catch(DataAccessException e) {
			verify(crateOperations, times(2)).execute(any(CrateSQLAction.class));
			throw e;
		}catch(Exception e) {
			fail("unknown error occured");
		}
	}
	
	@Test
	public void shouldNotStopSchemaExportOnError() throws Exception {
		
		initializeMappingContext(Person.class);
		
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new NoSuchTableException("table does not exist", null));
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new InvalidDataAccessResourceUsageException("Error!!"));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.setExportOption(CREATE);
		manager.setIgnoreFailures(true);
		manager.afterPropertiesSet();
		
		verify(crateOperations, times(2)).execute(any(CrateSQLAction.class));
	}
	
	@Test
	public void shouldDropCreateWhenTableDoesNotExist() throws Exception {
		
		initializeMappingContext(Person.class);
		
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new NoSuchTableException("table does not exist", null));
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.setExportOption(CREATE);
		manager.afterPropertiesSet();
		
		verify(crateOperations, times(2)).execute(any(CrateSQLAction.class));
	}
	
	@Test
	public void shouldDropCreateWhenTableExists() throws Exception {
		
		initializeMappingContext(Person.class);
		
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.setExportOption(CREATE);
		manager.afterPropertiesSet();
		
		verify(crateOperations, times(2)).execute(any(CrateSQLAction.class));
	}
	
	@Test
	public void shouldDropCreateAndDropOnDestructionWhenTableDoesNotExist() throws Exception {
		
		initializeMappingContext(Person.class);
		
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new NoSuchTableException("table does not exist", null)).
																 thenReturn(mock(SQLResponse.class));
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.setExportOption(CREATE_DROP);
		manager.afterPropertiesSet();
		manager.destroy();
		
		verify(crateOperations, times(3)).execute(any(CrateSQLAction.class));
	}
	
	@Test
	public void shouldDropCreateAndDropOnDestructionWhenTableExists() throws Exception {
		
		initializeMappingContext(Person.class);
		
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		doReturn(mock(SQLResponse.class)).when(crateOperations).execute(isA(CrateSQLAction.class));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.setExportOption(CREATE_DROP);
		manager.afterPropertiesSet();
		manager.destroy();
		
		verify(crateOperations, times(3)).execute(any(CrateSQLAction.class));
	}
	
	@Test
	public void shouldCreateTableOnlyWhenTableDoesNotExist() throws Exception {
		
		initializeMappingContext(Person.class);
		
		when(crateOperations.execute(isA(ColumnMetadataAction.class),
									 isA(ColumnMetadataAction.class))).thenThrow(new NoSuchTableException("table not found"));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.afterPropertiesSet();
		
		verify(crateOperations).execute(isA(CrateSQLAction.class));
	}
	
	@Test(expected=DataAccessException.class)
	public void shouldNotAlterTableOnError() throws Exception {
		
		initializeMappingContext(Person.class);
		
		List<ColumnMetadata> columns = asList(createColumnMetadata("['name']", STRING),
											  createColumnMetadata("['age']", INTEGER));
		
		when(crateOperations.execute(isA(ColumnMetadataAction.class),
									 isA(ColumnMetadataAction.class))).thenReturn(columns);
		when(crateOperations.execute(isA(CrateSQLAction.class))).thenThrow(new InvalidDataAccessResourceUsageException("Error!!"));
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.afterPropertiesSet();
	}
	
	@Test
	public void shouldNotAlterTableWhenTableAndEntityAreInSynch() throws Exception {
		
		initializeMappingContext(Person.class);
		
		List<ColumnMetadata> columns = asList(createColumnMetadata("name", STRING),
											  createColumnMetadata("age", INTEGER));
		
		when(crateOperations.execute(isA(ColumnMetadataAction.class),
									 isA(ColumnMetadataAction.class))).thenReturn(columns);
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.afterPropertiesSet();
		
		verify(crateOperations, never()).execute(isA(CrateSQLAction.class));
	}
	
	@Test
	public void shouldAlterTable() throws Exception {
		
		initializeMappingContext(Entity.class);
		
		List<ColumnMetadata> columns = asList(createColumnMetadata("stringField", STRING));
		
		when(crateOperations.execute(isA(ColumnMetadataAction.class),
									 isA(ColumnMetadataAction.class))).thenReturn(columns);
		
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateOperations);
		manager.afterPropertiesSet();
		
		verify(crateOperations).execute(isA(CrateSQLAction.class));
	}
	
	private ColumnMetadata createColumnMetadata(String sqlPath, String crateType) {
		return new ColumnMetadata(sqlPath, crateType);
	}
	
	private void initializeMappingContext(Class<?> type) {
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();
	}
	
	@Table(name="person")
	static class Person {
		private String name;
		private int age;
	}
	
	@Table(name="entity")
	static class Entity {
		private String stringField;
		private List<Integer> integers;
	}
}