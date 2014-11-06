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

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.data.crate.core.mapping.CrateDataType.BOOLEAN;
import static org.springframework.data.crate.core.mapping.CrateDataType.BYTE;
import static org.springframework.data.crate.core.mapping.CrateDataType.DOUBLE;
import static org.springframework.data.crate.core.mapping.CrateDataType.FLOAT;
import static org.springframework.data.crate.core.mapping.CrateDataType.INTEGER;
import static org.springframework.data.crate.core.mapping.CrateDataType.LONG;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.crate.core.mapping.CrateDataType.SHORT;
import static org.springframework.data.crate.core.mapping.CrateDataType.STRING;
import static org.springframework.data.crate.core.mapping.CrateDataType.TIMESTAMP;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.data.crate.InvalidCrateApiUsageException;
import org.springframework.data.crate.core.CyclicReferenceException;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntityTableDefinitionMapperTest.CollectionTypeTableMappingTest;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntityTableDefinitionMapperTest.EntityTypeTableMappingTest;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntityTableDefinitionMapperTest.MapTypeTableMappingTest;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntityTableDefinitionMapperTest.PrimitivesTableMappingTest;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntityTableDefinitionMapperTest.TableDefinitionMappingTest;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */

@RunWith(Suite.class)
@SuiteClasses({ TableDefinitionMappingTest.class, PrimitivesTableMappingTest.class,
				CollectionTypeTableMappingTest.class, MapTypeTableMappingTest.class,
				EntityTypeTableMappingTest.class })
public class CratePersistentEntityTableDefinitionMapperTest {
	
	/**
	 * 
	 * @author Hasnain Javed 
	 */
	@Ignore
	public static class TableDefinitionMappingTest {
		
		@Test
		public void shouldHaveTableNameFromClass() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(TestEntity.class);
			
			assertThat(tableDefinition, is(notNullValue()));
			assertThat(tableDefinition.getName(), is(TestEntity.class.getName()));
		}
		
		@Test
		public void shouldHaveTableNameFromAnnotation() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(AnnotatedTestEntity.class);
			
			assertThat(tableDefinition, is(notNullValue()));
			assertThat(tableDefinition.getName(), is(AnnotatedTestEntity.class.getAnnotation(Table.class).name()));
		}
		
		static class TestEntity {
		}
		
		@Table(name="test_entity")
		static class AnnotatedTestEntity {
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 */
	public static class PrimitivesTableMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithString.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			assertThat(tableDefinition.getColumns().iterator().next().getType(), is(STRING));
		}
		
		@Test
		public void shouldCreateDefinitionWithShortColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithShort.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(SHORT));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithByteColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithByte.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(BYTE));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithIntegerColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithInt.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(INTEGER));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithLongColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithLong.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(LONG));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithFloatColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithFloat.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(FLOAT));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithDoubleColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithDouble.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(DOUBLE));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithBooleanColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithBoolean.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(BOOLEAN));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithDateColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithDate.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			assertThat(tableDefinition.getColumns().iterator().next().getType(), is(TIMESTAMP));
		}
		
		static class EntityWithString {
			String string;
		}
		
		static class EntityWithShort {
			short primitive;
			Short wrapper;
		}
		
		static class EntityWithByte {
			byte primitive;
			Byte wrapper;
		}
		
		static class EntityWithInt {
			int primitive;
			Integer wrapper;
		}
		
		static class EntityWithLong {
			long primitive;
			Long wrapper;
		}
		
		static class EntityWithFloat {
			float primitive;
			Float wrapper;
		}
		
		static class EntityWithDouble {
			double primitive;
			Double wrapper;
		}
		
		static class EntityWithBoolean {
			boolean primitive;
			Boolean wrapper;
		}
		
		static class EntityWithDate {
			Date dateField;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed 
	 */
	public static class CollectionTypeTableMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringArrayColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithStringArray.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(STRING));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithShortArrayColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithShortCollection.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(SHORT));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithIntegerArrayColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithIntegerArray.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(INTEGER));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithLongArrayColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithLongCollection.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(LONG));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithFloatArrayColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithFloatCollection.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(FLOAT));
			}
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedCollectionTypes() {
			
			initMappingContextAndGetTableDefinition(EntityWithCollectionOfArray.class);
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedArrayTypes() {
			
			initMappingContextAndGetTableDefinition(EntityWithArrayOfCollection.class);
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedArrayTypesOfArray() {
			
			initMappingContextAndGetTableDefinition(EntityWithArrayOfArray.class);
		}
		
		static class EntityWithStringArray {
			String[] array;
		}
		
		static class EntityWithIntegerArray {
			int[] primitive;
			Integer[] wrapper;
		}
		
		static class EntityWithShortCollection {
			Collection<Short> collection;
		}
		
		static class EntityWithLongCollection {
			List<Long> collection;
		}
		
		static class EntityWithFloatCollection {
			Set<Float> collection;
		}
		
		static class EntityWithCollectionOfArray {
			Set<String[]> collection;
		}
		
		static class EntityWithArrayOfCollection {
			Set<String>[] array;
		}
		
		static class EntityWithArrayOfArray{
			Set<String>[][] array;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed 
	 */
	public static class MapTypeTableMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringMapColumn() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(EntityWithStringMap.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(1));
			
			for(Column column : tableDefinition.getColumns()) {
				assertThat(column.getType(), is(OBJECT));
			}
		}
		
		static class EntityWithStringMap {
			Map<String, String> map;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed 
	 */
	public static class EntityTypeTableMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithNestedEntity() {
			
			TableDefinition tableDefinition = initMappingContextAndGetTableDefinition(LevelOneEntity.class);
			
			assertThat(tableDefinition.getColumns(), is(notNullValue()));
			assertThat(tableDefinition.getColumns().size(), is(2));
			
			// level 1
			assertThat(tableDefinition.getColumns().get(0).getType(), is(STRING));
			assertThat(tableDefinition.getColumns().get(1).getType(), is(OBJECT));
			
			// level 2
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().size(), is(2));
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(0).getType(), is(STRING));
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(1).getType(), is(ARRAY));
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(1).getElementType(), is(OBJECT));
			
			// level 3
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(1).getSubColumns().size(), is(2));
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(1).getSubColumns().get(0).getType(), is(STRING));
			assertThat(tableDefinition.getColumns().get(1).getSubColumns().get(1).getSubColumns().get(1).getType(), is(OBJECT));
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectCycleForSelfReferencing() {
				initMappingContextAndGetTableDefinition(SelfReferencingEntity.class);
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectCycleForNestedClassViaCollectionReferencing() {
				initMappingContextAndGetTableDefinition(LevelOneCycle.class);
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectInBewteenCycleForNestedClassReferencing() {
				initMappingContextAndGetTableDefinition(LevelThreeCycle.class);
		}
		
		static class LevelOneEntity {
			String levelOneString;
			LevelTwoEntity levelTwo;
		}
		
		static class LevelTwoEntity {
			String levelTwoString;
			List<LevelThreeEntity> levelThrees;
		}
		
		static class LevelThreeEntity {
			String levelThreeString;
			Map<String, String> levelThreeMap;
		}
		
		static class SelfReferencingEntity {
			SelfReferencingEntity cyclic;
		}
		
		static class LevelOneCycle {
			LevelTwoCycle levelTwo;
		}
		
		static class LevelTwoCycle {
			Set<LevelOneCycle> levelOneCycles;
		}
		
		static class LevelThreeCycle {
			LevelTwoCycle levelTwo;
		}
	}
	
	private static TableDefinition initMappingContextAndGetTableDefinition(Class<?> type) {
		CrateMappingContext mappingContext = prepareMappingContext(type);
		return new CratePersistentEntityTableDefinitionMapper(mappingContext).
				   createDefinition(mappingContext.getPersistentEntity(type));
	}

	private static CrateMappingContext prepareMappingContext(Class<?> type) {

		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();

		return mappingContext;
	}
}