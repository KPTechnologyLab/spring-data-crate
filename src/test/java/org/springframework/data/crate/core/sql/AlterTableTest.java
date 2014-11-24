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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.schema.Column;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0 
 */
public class AlterTableTest {

	@Test
	public void shouldAddPrimitveColumnWithPrimaryKey() {
		
		Column longCol = createColumn("idField", Long.class, null, true);
		
		CrateSQLStatement statement = new AlterTable("testTable", longCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"idField\" long PRIMARY KEY"));
	}
	
	@Test
	public void shouldAddPrimitveColumn() {
		
		Column stringCol = createColumn("stringField", String.class, null, false);
		
		CrateSQLStatement statement = new AlterTable("testTable", stringCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"stringField\" string"));
	}
	
	@Test
	public void shouldAddPrimitveArrayColumn() {
		
		Column stringArrayCol = createColumn("stringArray", Set.class, String.class, false);
		
		CrateSQLStatement statement = new AlterTable("testTable", stringArrayCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"stringArray\" array(string)"));
	}
	
	@Test
	public void shouldAddMapColumn() {
		
		Column mapCol = createColumn("map", Map.class, null, false);
		
		CrateSQLStatement statement = new AlterTable("testTable", mapCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"map\" object"));
	}
	
	@Test
	public void shouldAddObjectColumn() {
		
		Column stringCol = createColumn("field1", String.class, null, false);
		Column objectCol = createColumn("Entity", Object.class, null, false);
		objectCol.setSubColumns(asList(stringCol));
		
		CrateSQLStatement statement = new AlterTable("testTable", objectCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"Entity\" object AS (\"field1\" string)"));
	}
	
	@Test
	public void shouldAddPrimitiveInObjectColumn() {
		
		Column stringCol = createColumn("Entity.field1", Integer.class, null, false);
		
		CrateSQLStatement statement = new AlterTable("testTable", stringCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"Entity\"['field1'] integer"));
	}
	
	@Test
	public void shouldAddPrimitiveArrayInObjectColumn() {
		
		Column arrayCol = createColumn("entity.longArray", Set.class, Long.class, false);
		
		CrateSQLStatement statement = new AlterTable("testTable", arrayCol);

		assertThat(statement.createStatement(), is("ALTER TABLE testTable ADD COLUMN \"entity\"['longArray'] array(long)"));
	}
	
	@Test
	public void shouldAddObjectArrayInNestedObjectColumn() {
		
		Column objectColOne = createColumn("strings", Collection.class, String.class, false);
		Column objectColTwo = createColumn("longField", Long.class, null, false);
		Column objectArrayCol = createColumn("entity.nested.objectArray", List.class, Object.class, false);
		objectArrayCol.setSubColumns(asList(objectColOne, objectColTwo));
		
		CrateSQLStatement statement = new AlterTable("testTable", objectArrayCol);
		
		StringBuilder sql = new StringBuilder("ALTER TABLE testTable ADD COLUMN \"entity\"['nested']['objectArray'] ");
		sql.append("array(object AS (\"strings\" array(string), \"longField\" long))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldAddObjectInNestedObjectArrayObjectColumn() {
		
		Column objectColOne = createColumn("map", Map.class, null, false);
		Column objectColTwo = createColumn("longs", Set.class, Long.class, false);
		Column objectCol = createColumn("entity.nested.objectArray.Element.object", Object.class, null, false);
		objectCol.setSubColumns(asList(objectColOne, objectColTwo));
		
		CrateSQLStatement statement = new AlterTable("testTable", objectCol);
		
		StringBuilder sql = new StringBuilder("ALTER TABLE testTable ADD COLUMN ");
		sql.append("\"entity\"['nested']['objectArray']['Element']['object'] ");
		sql.append("object AS (\"map\" object, \"longs\" array(long))");
		
		assertThat(statement.createStatement(), is(sql.toString()));
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
}