package org.springframework.data.crate.core.mapping.schema;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.data.crate.core.mapping.CrateDataType.DOUBLE;
import static org.springframework.data.crate.core.mapping.CrateDataType.INTEGER;
import static org.springframework.data.crate.core.mapping.CrateDataType.LONG;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.crate.core.mapping.CrateDataType.STRING;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;

public class CratePersistentEntityTableManagerTest {
	
	private CrateMappingContext mappingContext;
	private CratePersistentEntityTableManager tableManager;
	
	@Before()
	public void setup() {
		mappingContext = new CrateMappingContext();
		tableManager = new CratePersistentEntityTableManager(mappingContext);
	}
	
	@Test 
	public void shouldCreateTabelDefinition() {// Of Array,Collection,Object etc
		
		initMappingContext(Primitives.class);
		
		TableDefinition tableDefinition = tableManager.createDefinition(mappingContext.getPersistentEntity(Primitives.class));
		
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("primitives"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(8));
	}
	
	@Test
	public void shouldPickUpPrimitiveColumn() {
		
		TableMetadata tableMetadata = new TableMetadata("levelOne", asList(new ColumnMetadata("field1", STRING)));
		
		initMappingContext(LevelOne.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelOne.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelOne"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		assertThat(tableDefinition.getColumns().get(0).getName(), is("field2"));
		assertThat(tableDefinition.getColumns().get(0).getCrateType(), is(LONG));
	}
	
	@Test
	public void shouldPickUpPrimitiveArrayColumn() {
		
		TableMetadata tableMetadata = new TableMetadata("levelTwo", asList(new ColumnMetadata("integerField", INTEGER)));
		
		initMappingContext(LevelTwo.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelTwo.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelTwo"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		assertThat(tableDefinition.getColumns().get(0).getName(), is("stringArray"));
		assertThat(tableDefinition.getColumns().get(0).getCrateType(), is(ARRAY));
		assertThat(tableDefinition.getColumns().get(0).getElementCrateType(), is(STRING));
	}
	
	@Test
	public void shouldPickUpObjectColumn() { // of object array column
		
		TableMetadata tableMetadata = new TableMetadata("levelFour", asList(new ColumnMetadata("strings", ARRAY)));
		
		initMappingContext(LevelFour.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelFour.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelFour"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		
		Column objectColumn = tableDefinition.getColumns().get(0); 
		assertThat(objectColumn.getName(), is("two"));
		assertThat(objectColumn.getCrateType(), is(OBJECT));
		assertThat(objectColumn.getSubColumns().isEmpty(), is(false));
		assertThat(objectColumn.getSubColumns().size(), is(2));
	}
	
	@Test
	public void shouldPickUpNestedPrimitiveColumn() {
		
		ColumnMetadata levelThreeCol1 = new ColumnMetadata("doubleField", DOUBLE);
		ColumnMetadata levelThreeCol2 = new ColumnMetadata("two", OBJECT);
		ColumnMetadata levelTwoCol = new ColumnMetadata("two.stringArray", ARRAY);
		
		TableMetadata tableMetadata = new TableMetadata("levelThree", asList(levelThreeCol1, levelThreeCol2, levelTwoCol));
		
		initMappingContext(LevelThree.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelThree.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelThree"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		assertThat(tableDefinition.getColumns().get(0).getName(), is("integerField"));
		assertThat(tableDefinition.getColumns().get(0).getCrateType(), is(INTEGER));
	}
	
	@Test
	public void shouldPickUpNestedArrayColumn() {
		
		ColumnMetadata levelThreeCol1 = new ColumnMetadata("doubleField", DOUBLE);
		ColumnMetadata levelThreeCol2 = new ColumnMetadata("two", OBJECT);
		ColumnMetadata levelTwoCol = new ColumnMetadata("two.integerField", INTEGER);
		
		TableMetadata tableMetadata = new TableMetadata("levelThree", asList(levelThreeCol1, levelThreeCol2, levelTwoCol));
		
		initMappingContext(LevelThree.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelThree.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelThree"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		assertThat(tableDefinition.getColumns().get(0).getName(), is("stringArray"));
		assertThat(tableDefinition.getColumns().get(0).getCrateType(), is(ARRAY));
		assertThat(tableDefinition.getColumns().get(0).getElementCrateType(), is(STRING));
	}
	
	@Test
	public void shouldPickUpNestedArrayObjectPrimitiveColumn() {
		
		ColumnMetadata levelOneCol1 = new ColumnMetadata("levelFives.levelOnes.field1", STRING);
		ColumnMetadata levelFiveCol1 = new ColumnMetadata("levelFives.levelOnes", ARRAY, OBJECT);
		ColumnMetadata levelSixCol1 = new ColumnMetadata("levelFives", ARRAY, OBJECT);
		
		TableMetadata tableMetadata = new TableMetadata("levelSix", asList(levelSixCol1, levelFiveCol1, levelOneCol1));
		
		initMappingContext(LevelFive.class);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(mappingContext.getPersistentEntity(LevelSix.class),
																		tableMetadata);
		assertThat(tableDefinition, is(notNullValue()));
		assertThat(tableDefinition.getName(), is("levelSix"));
		assertThat(tableDefinition.getColumns().isEmpty(), is(false));
		assertThat(tableDefinition.getColumns().size(), is(1));
		assertThat(tableDefinition.getColumns().get(0).getName(), is("field2"));
		assertThat(tableDefinition.getColumns().get(0).getCrateType(), is(LONG));
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
	
	@Table(name="levelOne")
	static class LevelOne {
		String field1;
		Long field2;
	}
	
	@Table(name="levelTwo")
	static class LevelTwo {
		int integerField;
		String[] stringArray;
	}
	
	@Table(name="levelThree")
	static class LevelThree {
		double doubleField;
		LevelTwo two;
	}
	
	@Table(name="levelFour")
	static class LevelFour {
		List<String> strings;
		LevelTwo two;
	}
	
	@Table(name="levelFive")
	static class LevelFive {
		Set<LevelOne> levelOnes;
	}
	
	@Table(name="levelSix")
	static class LevelSix {
		Set<LevelFive> levelFives;
	}
}