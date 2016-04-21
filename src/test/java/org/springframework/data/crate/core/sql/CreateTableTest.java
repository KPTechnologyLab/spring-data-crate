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
import static org.springframework.data.crate.core.mapping.schema.ColumnPloicy.STRICT;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.Column;
import org.springframework.data.crate.core.mapping.schema.TableDefinition;
import org.springframework.data.crate.core.mapping.schema.TableParameters;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CreateTableTest {

	@Test
	public void shouldCreateStatementWithPrimaryKeyColumn() {
		
		Column longCol = createColumn("longField", Long.class, null, true);
		TableDefinition tableDefinition = createTableDefinition("entity", null, longCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"longField\" long PRIMARY KEY)"));
	}
	
	@Test
	public void shouldCreateStatementWithObjectAsPrimaryKeyColumn() {
		
		Column intCol = createColumn("intField", Integer.class, null, null);
		Column stringCol = createColumn("stringField", String.class, null, null);
		Column pk = createColumn("pk", EntityWithCompositePrimaryKey.class, null, true);
		pk.setSubColumns(asList(intCol, stringCol));
		
		Column longCol = createColumn("longField", Long.class, null, false);
		TableDefinition tableDefinition = createTableDefinition("entity", null, pk, longCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"pk\" object AS (");
		sql.append("\"intField\" integer PRIMARY KEY, \"stringField\" string PRIMARY KEY), \"longField\" long)");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldCreateStatementWithPrimitiveColumn() {
		
		Column intCol = createColumn("integerField", Integer.class, null, false);
		TableDefinition tableDefinition = createTableDefinition("entity", null, intCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"integerField\" integer)"));
	}
	
	@Test
	public void shouldCreateStatementWithPrimitiveCollection() {
		
		Column arrayCol = createColumn("integers", Integer[].class, Integer.class, null);
		TableDefinition tableDefinition = createTableDefinition("entity", null, arrayCol);

		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		assertThat(statement.createStatement(), is("CREATE TABLE entity (\"entity_class\" string, \"integers\" array(integer))"));
	}
	
	@Test
	public void shouldCreateStatementWithMap() {
		
		Column mapCol = createColumn("map", Map.class, null, null);
		TableDefinition tableDefinition = createTableDefinition("entity", null, mapCol);
		
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
		
		TableDefinition tableDefinition = createTableDefinition("entity", null, rootStringCol, objectCol);
		
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
		
		TableDefinition tableDefinition = createTableDefinition("entity", null, rootStringCol, objectCol);
		
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
		
		TableDefinition tableDefinition = createTableDefinition("entity", null, rootStringCol, rootArrayCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"stringField\" string, \"nestedEntities\" array(");
		sql.append("object AS (\"stringField\" string, \"nested\" object AS (");
		sql.append("\"stringField\" string, \"integerField\" integer))))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldCreateStatementWithTableParameters() {
		
		Column stringCol = createColumn("field", String.class, null, false);
		TableParameters parameters = new TableParameters("2", 1500, STRICT);
		TableDefinition tableDefinition = createTableDefinition("entity", parameters, stringCol);
		
		CrateSQLStatement statement = new CreateTable(tableDefinition);
		
		StringBuilder sql = new StringBuilder("CREATE TABLE entity (\"entity_class\" string, \"field\" string) ");
		sql.append("WITH (number_of_replicas='2', refresh_interval='1500', column_policy='strict')");
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	private TableDefinition createTableDefinition(String name, TableParameters parameters, Column... columns) {
		return new TableDefinition(name, asList(columns), parameters);
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
	
	@Table(name="entity")
	static class EntityWithCompositePrimaryKey {
		@Id
		SimplePrimaryKey pk;
	}
	
	static class SimplePrimaryKey {
		
		int intField;
		String stringField;
	}
}