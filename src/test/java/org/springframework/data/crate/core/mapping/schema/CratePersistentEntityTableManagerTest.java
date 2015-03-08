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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.data.crate.core.mapping.CrateDataType.LONG;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.crate.core.mapping.CrateDataType.STRING;
import static org.springframework.data.crate.core.mapping.schema.ColumnPloicy.DYNAMIC;
import static org.springframework.data.crate.core.sql.CrateSQLStatement.NO_OF_REPLICAS;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.AlterTableDefinition.AlterTableParameterDefinition;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CratePersistentEntityTableManagerTest {
	
	public static final TableParameters DEFAULT_PARAMS = new TableParameters("1", 1000, DYNAMIC);
	
	private CrateMappingContext mappingContext;
	private CratePersistentEntityTableManager tableManager;
	
	@Before()
	public void setup() {
		mappingContext = new CrateMappingContext();
		tableManager = new CratePersistentEntityTableManager(mappingContext);
	}
	
	@Test 
	public void shouldCreateTabelDefinitionWithPrimitives() {
		
		initMappingContext(Primitives.class);
		
		TableDefinition def = tableManager.createDefinition(mappingContext.getPersistentEntity(Primitives.class));
		
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("primitives"));
		assertThat(def.getColumns().size(), is(8));
	}
	
	@Test 
	public void shouldCreateTabelDefinitionWithPrimitivesCollection() {
		
		initMappingContext(PrimitveCollectionTypes.class);
		
		TableDefinition def = tableManager.createDefinition(mappingContext.getPersistentEntity(PrimitveCollectionTypes.class));
		
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("PrimitveCollectionTypes"));
		assertThat(def.getColumns().size(), is(2));
	}
	
	@Test 
	public void shouldCreateTabelDefinitionWithEntity() {
		
		initMappingContext(Entity.class);
		
		TableDefinition def = tableManager.createDefinition(mappingContext.getPersistentEntity(Entity.class));
		
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("entity"));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getSubColumns().size(), is(2));
	}
	
	@Test 
	public void shouldCreateTabelDefinitionWithEntityCollectionTypes() {
		
		initMappingContext(EntityCollectionTypes.class);
		
		TableDefinition tableDefinition = tableManager.createDefinition(mappingContext.getPersistentEntity(EntityCollectionTypes.class));
		
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("entityCollectionTypes"));
		assertThat(tableDefinition.getColumns().size(), is(2));
		assertThat(tableDefinition.getColumns().get(0).getSubColumns().size(), is(2));
		assertThat(tableDefinition.getColumns().get(1).getSubColumns().size(), is(2));
	}
	
	@Test 
	public void shouldCreateTabelDefinitionWithMap() {
		
		initMappingContext(EntityWithMap.class);
		
		TableDefinition def = tableManager.createDefinition(mappingContext.getPersistentEntity(EntityWithMap.class));
		
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("entityWithMap"));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getSubColumns().isEmpty(), is(true));
	}
	
	@Test
	public void shouldPickUpColumn() {
		
		TableMetadata tableMetadata = new TableMetadata("levelZero", asList(new ColumnMetadata("field1", STRING)), 
														DEFAULT_PARAMS);
		
		initMappingContext(LevelZero.class);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(LevelZero.class),
																tableMetadata);
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("levelZero"));
		assertThat(def.getColumns().isEmpty(), is(false));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getName(), is("field2"));
		assertThat(def.getColumns().get(0).getCrateType(), is(LONG));
	}
	
	@Test
	public void shouldPickUpObjectColumn() {
		
		TableMetadata tableMetadata = new TableMetadata("levelTwo", asList(new ColumnMetadata("stringArray", ARRAY, STRING)), 
										  DEFAULT_PARAMS);
		
		initMappingContext(LevelTwo.class);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(LevelTwo.class),
																tableMetadata);
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("levelTwo"));
		assertThat(def.getColumns().isEmpty(), is(false));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getName(), is("one"));
		assertThat(def.getColumns().get(0).getCrateType(), is(OBJECT));
	}
	
	@Test
	public void shouldPickUpObjectArrayColumn() {
		
		TableMetadata tableMetadata = new TableMetadata("levelFour", asList(new ColumnMetadata("field1", STRING)), 
														DEFAULT_PARAMS);
		
		initMappingContext(LevelFour.class);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(LevelFour.class),
															    tableMetadata);
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("levelFour"));
		assertThat(def.getColumns().isEmpty(), is(false));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getName(), is("zeros"));
		assertThat(def.getColumns().get(0).getCrateType(), is(ARRAY));
		assertThat(def.getColumns().get(0).getElementCrateType(), is(OBJECT));
	}
	
	@Test
	public void shouldPickUpColumnOnLevelZero() {
		
		ColumnMetadata levelZeroCol = new ColumnMetadata("zero.field1", STRING);
		ColumnMetadata levelOneCol = new ColumnMetadata("zero", OBJECT);
		
		TableMetadata tableMetadata = new TableMetadata("levelOne", asList(levelOneCol, levelZeroCol), 
														DEFAULT_PARAMS);
		
		initMappingContext(LevelOne.class);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(LevelOne.class),
															    tableMetadata);
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("levelOne"));
		assertThat(def.getColumns().isEmpty(), is(false));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getName(), is("zero.field2"));
		assertThat(def.getColumns().get(0).getCrateType(), is(LONG));
	}
	
	@Test
	public void shouldPickUpColumnOfArrayObjectOnLevelThreeInLevelZero() {
		
		ColumnMetadata levelZeroCol1 = new ColumnMetadata("twos.one.zero.field2", LONG);
		ColumnMetadata levelOneCol1 = new ColumnMetadata("twos.one.zero", OBJECT);
		ColumnMetadata levelTwoCol1 = new ColumnMetadata("twos.stringArray", ARRAY, STRING);
		ColumnMetadata levelTwoCol2 = new ColumnMetadata("twos.one", OBJECT);
		ColumnMetadata levelThreeCol = new ColumnMetadata("twos", ARRAY, OBJECT);
		
		TableMetadata tableMetadata = new TableMetadata("levelThree", asList(levelThreeCol, levelTwoCol1, 
																			 levelTwoCol2, levelOneCol1, levelZeroCol1),
														DEFAULT_PARAMS);
		
		initMappingContext(LevelThree.class);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(LevelThree.class),
																tableMetadata);
		assertThat(def, is(notNullValue()));
		assertThat(def.getName(), is("levelThree"));
		assertThat(def.getColumns().isEmpty(), is(false));
		assertThat(def.getColumns().size(), is(1));
		assertThat(def.getColumns().get(0).getName(), is("twos.one.zero.field1"));
		assertThat(def.getColumns().get(0).getCrateType(), is(STRING));
	}
	
	@Test
	public void shouldPickUpChangedTableParameters() {
		
		TableMetadata tableMetadata = new TableMetadata("entity", asList(new ColumnMetadata("field1", STRING)), DEFAULT_PARAMS);
		
		AlterTableDefinition def = tableManager.alterDefinition(mappingContext.getPersistentEntity(TableParams.class),
																tableMetadata);
		
		assertThat(def, is(notNullValue()));
		assertThat(def.hasAlteredParameters(), is(true));
		
		AlterTableParameterDefinition paramDef = def.getTableParameters().iterator().next(); 
		assertThat(paramDef.getParameterName(), is(NO_OF_REPLICAS));
		assertThat(paramDef.getParameterValue().toString(), is("2"));
	}

	private void initMappingContext(Class<?> clazz) {
		mappingContext.setInitialEntitySet(singleton(clazz));
		mappingContext.initialize();
	}
	
	@Table(name="primitives")
	static class Primitives {
		String stringField;
		short shortField;
		byte byteField;
		int intField;
		Long longWrapper;
		float floatField;
		double doubleField;
		Date date;
	}
	
	@Table(name="PrimitveCollectionTypes")
	static class PrimitveCollectionTypes {
		String[] stringArray;
		List<String> stringCollection;
	}
	
	@Table(name="entity")
	static class Entity {
		LevelZero zero;
	}
	
	@Table(name="entityCollectionTypes")
	static class EntityCollectionTypes {
		Set<LevelZero> zerosCollection;
		LevelZero[] zerosArray;
	}
	
	@Table(name="entityWithMap")
	static class EntityWithMap {
		Map<String, Primitives> map;
	}
	
	@Table(name="levelZero")
	static class LevelZero {
		String field1;
		Long field2;
	}
	
	@Table(name="levelOne")
	static class LevelOne {
		LevelZero zero;
	}
	
	@Table(name="levelTwo")
	static class LevelTwo {
		String[] stringArray;
		LevelOne one;
	}
	
	@Table(name="levelThree")
	static class LevelThree {
		List<LevelTwo> twos;
	}
	
	@Table(name="levelFour")
	static class LevelFour {
		String field1;
		List<LevelZero> zeros;
	}
	
	@Table(name="entity", numberOfReplicas="2")
	public class TableParams {
		String field1;
	}
}