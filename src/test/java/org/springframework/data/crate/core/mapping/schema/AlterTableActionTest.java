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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.data.crate.core.CrateSQLAction;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AlterTableActionTest {
	
	@Test
	public void shouldAddPrimitveColumnWithPrimaryKey() {
		
		Column longCol = createColumn("idField", Long.class, null, true);
		
		CrateSQLAction action = new AlterTableAction("testTable", longCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"idField\" long primary key"));
	}
	
	@Test
	public void shouldAddPrimitveColumn() {
		
		Column stringCol = createColumn("stringField", String.class, null, false);
		
		CrateSQLAction action = new AlterTableAction("testTable", stringCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"stringField\" string"));
	}
	
	@Test
	public void shouldAddPrimitveArrayColumn() {
		
		Column stringArrayCol = createColumn("stringArray", Set.class, String.class, false);
		
		CrateSQLAction action = new AlterTableAction("testTable", stringArrayCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"stringArray\" array(string)"));
	}
	
	@Test
	public void shouldAddMapColumn() {
		
		Column mapCol = createColumn("map", Map.class, null, false);
		
		CrateSQLAction action = new AlterTableAction("testTable", mapCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"map\" object"));
	}
	
	@Test
	public void shouldAddObjectColumn() {
		
		Column stringCol = createColumn("field1", String.class, null, false);
		Column objectCol = createColumn("Entity", Object.class, null, false);
		objectCol.setSubColumns(asList(stringCol));
		
		CrateSQLAction action = new AlterTableAction("testTable", objectCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"Entity\" object as (\"field1\" string)"));
	}
	
	@Test
	public void shouldAddPrimitiveInObjectColumn() {
		
		Column stringCol = createColumn("Entity.field1", Integer.class, null, false);
		
		CrateSQLAction action = new AlterTableAction("testTable", stringCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"Entity\"['field1'] integer"));
	}
	
	@Test
	public void shouldAddPrimitiveArrayInObjectColumn() {
		
		Column arrayCol = createColumn("entity.longArray", Set.class, Long.class, false);
		
		CrateSQLAction action = new AlterTableAction("testTable", arrayCol);

		assertThat(action.getSQLStatement(), is("alter table testTable add column \"entity\"['longArray'] array(long)"));
	}
	
	@Test
	public void shouldAddObjectArrayInNestedObjectColumn() {
		
		Column objectColOne = createColumn("strings", Collection.class, String.class, false);
		Column objectColTwo = createColumn("longField", Long.class, null, false);
		Column objectArrayCol = createColumn("entity.nested.objectArray", List.class, Object.class, false);
		objectArrayCol.setSubColumns(asList(objectColOne, objectColTwo));
		
		CrateSQLAction action = new AlterTableAction("testTable", objectArrayCol);
		
		StringBuilder sql = new StringBuilder("alter table testTable add column \"entity\"['nested']['objectArray'] ");
		sql.append("array(object as (\"strings\" array(string), \"longField\" long))");
		
		assertThat(action.getSQLStatement(), is(sql.toString()));
	}
	
	@Test
	public void shouldAddObjectInNestedObjectArrayObjectColumn() {
		
		Column objectColOne = createColumn("map", Map.class, null, false);
		Column objectColTwo = createColumn("longs", Set.class, Long.class, false);
		Column objectCol = createColumn("entity.nested.objectArray.Element.object", Object.class, null, false);
		objectCol.setSubColumns(asList(objectColOne, objectColTwo));
		
		CrateSQLAction action = new AlterTableAction("testTable", objectCol);
		
		StringBuilder sql = new StringBuilder("alter table testTable add column ");
		sql.append("\"entity\"['nested']['objectArray']['Element']['object'] ");
		sql.append("object as (\"map\" object, \"longs\" array(long))");
		
		assertThat(action.getSQLStatement(), is(sql.toString()));
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