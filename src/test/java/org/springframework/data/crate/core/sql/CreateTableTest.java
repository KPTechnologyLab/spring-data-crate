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
package org.springframework.data.crate.core.sql;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.Column;
import org.springframework.data.crate.core.mapping.schema.TableDefinition;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CreateTableTest {

	@Test
	public void shouldCreateStatementWithPrimaryKeyColumn() {
		
		Column longCol = createColumn("longField", Long.class, null, true);
		TableDefinition tableDefinition = createTableDefinition("entity", longCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"longField\" long PRIMARY KEY)"));
	}
	
	@Test
	public void shouldCreateStatementWithPrimitiveColumn() {
		
		Column stringCol = createColumn("stringField", String.class, null, true);
		Column intCol = createColumn("integerField", Integer.class, null, false);
		TableDefinition tableDefinition = createTableDefinition("entity", stringCol, intCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"stringField\" string PRIMARY KEY, \"integerField\" integer)"));
	}
	
	@Test
	public void shouldCreateStatementWithPrimitiveCollection() {
		
		Column arrayCol = createColumn("integers", Integer[].class, Integer.class, null);
		TableDefinition tableDefinition = createTableDefinition("entity", arrayCol);

		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"integers\" array(integer))"));
	}
	
	@Test
	public void shouldCreateStatementWithMap() {
		
		Column mapCol = createColumn("map", Map.class, null, null);
		TableDefinition tableDefinition = createTableDefinition("entity", mapCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"map\" object)"));
	}
	
	@Test
	public void shouldCreateStatementWithNestedEntity() {
		
		Column stringCol = createColumn("stringField", String.class, null, null);
		Column intCol = createColumn("integerField", Integer.class, null, null);
		Column objectCol = createColumn("nestedEntity", EntityWithPrimitives.class, null, null);
		objectCol.setSubColumns(asList(stringCol, intCol));
		
		Column rootStringCol = createColumn("stringField", String.class, null, null);
		
		TableDefinition tableDefinition = createTableDefinition("entity", rootStringCol, objectCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"stringField\" string, \"nestedEntity\" object AS (");
		sql.append("\"stringField\" string, \"integerField\" integer))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldCreateStatementWithEntityArray() {
		
		Column stringCol = createColumn("stringField", String.class, null, null);
		Column intCol = createColumn("integerField", Integer.class, null, null);
		Column objectCol = createColumn("nestedEntities", EntityWithPrimitives[].class, EntityWithPrimitives.class, null);
		objectCol.setSubColumns(asList(stringCol, intCol));
		
		Column rootStringCol = createColumn("stringField", String.class, null, null);
		
		TableDefinition tableDefinition = createTableDefinition("entity", rootStringCol, objectCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"stringField\" string, \"nestedEntities\" array(");
		sql.append("object AS (\"stringField\" string, \"integerField\" integer)))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldCreateStatementWithNestedEntityCollection() {
		
		Column stringColLevel2 = createColumn("stringField", String.class, null, null);
		Column intColLevel2 = createColumn("integerField", Integer.class, null, null);
		
		Column stringColLevel1 = createColumn("stringField", String.class, null, null);
		Column objectColLevel1 = createColumn("nested", EntityWithPrimitives.class, null, null);
		objectColLevel1.setSubColumns(asList(stringColLevel2, intColLevel2));
		
		Column rootStringCol = createColumn("stringField", String.class, null, null);
		Column rootArrayCol = createColumn("nestedEntities", Set.class, EntityWithNestedEntity.class, null);
		rootArrayCol.setSubColumns(asList(stringColLevel1, objectColLevel1));
		
		TableDefinition tableDefinition = createTableDefinition("entity", rootStringCol, rootArrayCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"stringField\" string, \"nestedEntities\" array(");
		sql.append("object AS (\"stringField\" string, \"nested\" object AS (");
		sql.append("\"stringField\" string, \"integerField\" integer))))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	private TableDefinition createTableDefinition(String name, Column... columns) {
		return new TableDefinition(name, asList(columns));
	}
	
	private Column createColumn(String name, Class<?> type, Class<?> elementType, Boolean primaryKey) {
		Column column = null;
		if(elementType != null) {
			column = new Column(name, type, elementType);
		}else {
			column = new Column(name, type);
		}
		if(primaryKey != null) {
			column.setPrimaryKey(primaryKey);
		}
		return column;
	}
	
	@Table(name="entity")
	static class EntityWithPrimitives {
		String stringField;
		int integerField;
	}
	
	@Table(name="entity")
	static class EntityWithNestedEntity {
		String stringField;
		EntityWithPrimitives nested;
	}
	
	@Table(name="entity")
	static class EntityWithEntityCollection {
		String stringField;
		Set<EntityWithPrimitives> nestedEntities;
	}
	
	@Table(name="entity")
	static class EntityWithNestedEntityCollection {
		String stringField;
		Set<EntityWithNestedEntity> nestedEntities;
	}
}