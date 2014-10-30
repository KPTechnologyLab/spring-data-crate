package org.springframework.data.crate.core.mapping;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.data.sample.entities.Author;
import org.springframework.data.sample.entities.Book;

public class ColumnTest {
	
	private static final String STRING_COL_STMNT = "string_col string";
	private static final String STRING_COL_PK_STMNT = STRING_COL_STMNT.concat(" primary key");
	private static final String STRING_ARRAY_COL_STMNT = "string_array array(string)";
	private static final String STRING_COLLECTION_COL_STMNT = "string_collection array(string)";
	private static final String MAP_COL_STMNT = "map object";
	private static final String OBJECT_COL_STMNT = "obj object";
	private static final String OBJECT_WITH_FIELDS_COL_STMNT = "book object as (id integer, title string, isbn string)";
	private static final String NESTED_OBJECT_COL_STMNT = "author object as (name string, age integer, book object as (id integer, title string, isbn string))";
	private static final String OBJECT_ARRAY_COL_STMNT = "books_array array(object as (id integer, title string, isbn string))";
	private static final String OBJECT_COLLECTION_COL_STMNT = "books_collection array(object as (id integer, title string, isbn string))";
	private static final String NESTED_OBJECT_ARRAY_COL_STMNT = "authors_array array(object as (name string, age integer, book object as (id integer, title string, isbn string)))";
	private static final String NESTED_OBJECT_COLLECTION_COL_STMNT = "authors_collection array(object as (name string, age integer, book object as (id integer, title string, isbn string)))";
	
	@Test
	public void shouldCreateStatementForPrimitive() {
		
		Column column = new Column("string_col", String.class);
		
		assertThat(column.toSqlStatement(), is(STRING_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForPrimitiveAsPrimaryKey() {
		
		Column column = new Column("string_col", String.class);
		column.setPrimaryKey(true);
		
		assertThat(column.toSqlStatement(), is(STRING_COL_PK_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForPrimitiveArray() {
		
		Column column = new Column("string_array", String[].class);
		column.setElementType("string");
		
		assertThat(column.toSqlStatement(), is(STRING_ARRAY_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForPrimitiveCollection() {
		
		Column column = new Column("string_collection", Set.class);
		column.setElementType("string");
		
		assertThat(column.toSqlStatement(), is(STRING_COLLECTION_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForMap() {
		
		Column column = new Column("map", Map.class);
		
		assertThat(column.toSqlStatement(), is(MAP_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForEntityWithNoFields() {
		
		Column column = new Column("obj", EntityWithNoFields.class);
		
		assertThat(OBJECT_COL_STMNT, is(column.toSqlStatement()));
	}
	
	@Test
	public void shouldCreateStatementForEntity() {
		
		List<Column> subColumns = asList(new Column("id", Integer.class), 
										 new Column("title", String.class), 
										 new Column("isbn", String.class));
		
		Column column = new Column("book", Book.class);
		column.setSubColumns(subColumns);
		
		assertThat(column.toSqlStatement(), is(OBJECT_WITH_FIELDS_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForNestedEntities() {
		
		List<Column> bookSubColumns = asList(new Column("id", Integer.class), 
											 new Column("title", String.class), 
											 new Column("isbn", String.class));
		
		Column bookSubColumn = new Column("book", Book.class);
		bookSubColumn.setSubColumns(bookSubColumns);
		
		List<Column> authorSubColumns = asList(new Column("name", String.class), 
											   new Column("age", Integer.class),
											   bookSubColumn);
		
		Column authorColumn = new Column("author", Author.class);
		authorColumn.setSubColumns(authorSubColumns);
		
		assertThat(authorColumn.toSqlStatement(), is(NESTED_OBJECT_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForEntityArray() {
		
		List<Column> subColumns = asList(new Column("id", Integer.class), 
										 new Column("title", String.class), 
										 new Column("isbn", String.class));
		
		Column column = new Column("books_array", Book[].class);
		column.setElementType("object");
		column.setSubColumns(subColumns);
		
		assertThat(column.toSqlStatement(), is(OBJECT_ARRAY_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForEntityCollection() {
		
		List<Column> subColumns = asList(new Column("id", Integer.class), 
										 new Column("title", String.class), 
										 new Column("isbn", String.class));
		
		Column column = new Column("books_collection", List.class);
		column.setElementType("object");
		column.setSubColumns(subColumns);
		
		assertThat(column.toSqlStatement(), is(OBJECT_COLLECTION_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForNestedEntitiesArray() {
		
		List<Column> bookSubColumns = asList(new Column("id", Integer.class), 
											 new Column("title", String.class), 
											 new Column("isbn", String.class));
		
		Column bookSubColumn = new Column("book", Book.class);
		bookSubColumn.setSubColumns(bookSubColumns);
		
		List<Column> authorSubColumns = asList(new Column("name", String.class), 
											   new Column("age", Integer.class),
											   bookSubColumn);
		
		Column authorColumn = new Column("authors_array", Author[].class);
		authorColumn.setElementType("object");
		authorColumn.setSubColumns(authorSubColumns);
		
		assertThat(authorColumn.toSqlStatement(), is(NESTED_OBJECT_ARRAY_COL_STMNT));
	}
	
	@Test
	public void shouldCreateStatementForNestedEntitiesCollection() {
		
		List<Column> bookSubColumns = asList(new Column("id", Integer.class), 
											 new Column("title", String.class), 
											 new Column("isbn", String.class));
		
		Column bookSubColumn = new Column("book", Book.class);
		bookSubColumn.setSubColumns(bookSubColumns);
		
		List<Column> authorSubColumns = asList(new Column("name", String.class), 
											   new Column("age", Integer.class),
											   bookSubColumn);
		
		Column authorColumn = new Column("authors_collection", Collection.class);
		authorColumn.setElementType("object");
		authorColumn.setSubColumns(authorSubColumns);
		
		assertThat(authorColumn.toSqlStatement(), is(NESTED_OBJECT_COLLECTION_COL_STMNT));
	}
	
	private class EntityWithNoFields {
	}
}