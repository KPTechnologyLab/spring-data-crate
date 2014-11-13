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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.data.crate.InvalidCrateApiUsageException;
import org.springframework.data.crate.core.CyclicReferenceException;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.schema.EntityColumnMapperTest.CollectionTypeColumnMappingTest;
import org.springframework.data.crate.core.mapping.schema.EntityColumnMapperTest.EntityTypeColumnMappingTest;
import org.springframework.data.crate.core.mapping.schema.EntityColumnMapperTest.MapTypeColumnMappingTest;
import org.springframework.data.crate.core.mapping.schema.EntityColumnMapperTest.PrimitivesColumnMappingTest;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */

@RunWith(Suite.class)
@SuiteClasses({ PrimitivesColumnMappingTest.class, CollectionTypeColumnMappingTest.class, 
				MapTypeColumnMappingTest.class, EntityTypeColumnMappingTest.class })
public class EntityColumnMapperTest {

	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	public static class PrimitivesColumnMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithString.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			assertThat(columns.iterator().next().getType(), is(STRING));
		}
		
		@Test
		public void shouldCreateDefinitionWithShortColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithShort.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(SHORT));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithByteColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithByte.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(BYTE));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithIntegerColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithInt.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(INTEGER));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithLongColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithLong.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(LONG));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithFloatColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithFloat.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(FLOAT));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithDoubleColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithDouble.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(DOUBLE));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithBooleanColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithBoolean.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(BOOLEAN));	
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithDateColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithDate.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			assertThat(columns.iterator().next().getType(), is(TIMESTAMP));
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
	 * @since 1.0.0 
	 */
	public static class CollectionTypeColumnMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringArrayColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithStringArray.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(STRING));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithShortArrayColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithShortCollection.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(SHORT));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithIntegerArrayColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithIntegerArray.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(INTEGER));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithLongArrayColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithLongCollection.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(LONG));
			}
		}
		
		@Test
		public void shouldCreateDefinitionWithFloatArrayColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithFloatCollection.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			
			for(Column column : columns) {
				assertThat(column.getType(), is(ARRAY));
				assertThat(column.getElementType(), is(FLOAT));
			}
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedCollectionTypes() {
			
			initMappingContextAndGetColumns(EntityWithCollectionOfArray.class);
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedArrayTypes() {
			
			initMappingContextAndGetColumns(EntityWithArrayOfCollection.class);
		}
		
		@Test(expected=InvalidCrateApiUsageException.class)
		public void shouldNotCreateDefinitionForNestedArrayTypesOfArray() {
			
			initMappingContextAndGetColumns(EntityWithArrayOfArray.class);
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
	 * @since 1.0.0 
	 */
	public static class MapTypeColumnMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithStringMapColumn() {
			
			List<Column> columns = initMappingContextAndGetColumns(EntityWithStringMap.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(1));
			
			for(Column column : columns) {
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
	 * @since 1.0.0 
	 */
	public static class EntityTypeColumnMappingTest {
		
		@Test
		public void shouldCreateDefinitionWithNestedEntity() {
			
			List<Column> columns = initMappingContextAndGetColumns(LevelOneEntity.class);
			
			assertThat(columns, is(notNullValue()));
			assertThat(columns.size(), is(2));
			
			// level 1
			assertThat(columns.get(0).getType(), is(STRING));
			assertThat(columns.get(1).getType(), is(OBJECT));
			
			// level 2
			assertThat(columns.get(1).getSubColumns().size(), is(2));
			assertThat(columns.get(1).getSubColumns().get(0).getType(), is(STRING));
			assertThat(columns.get(1).getSubColumns().get(1).getType(), is(ARRAY));
			assertThat(columns.get(1).getSubColumns().get(1).getElementType(), is(OBJECT));
			
			// level 3
			assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().size(), is(2));
			assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().get(0).getType(), is(STRING));
			assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().get(1).getType(), is(OBJECT));
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectCycleForSelfReferencing() {
				initMappingContextAndGetColumns(SelfReferencingEntity.class);
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectCycleForNestedClassViaCollectionReferencing() {
				initMappingContextAndGetColumns(LevelOneCycle.class);
		}
		
		@Test(expected=CyclicReferenceException.class)
		public void shouldDetectInBewteenCycleForNestedClassReferencing() {
				initMappingContextAndGetColumns(LevelThreeCycle.class);
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
	
	private static List<Column> initMappingContextAndGetColumns(Class<?> type) {
		CrateMappingContext mappingContext = prepareMappingContext(type);
		return new EntityColumnMapper(mappingContext).createColumns(mappingContext.getPersistentEntity(type)); 
	}

	private static CrateMappingContext prepareMappingContext(Class<?> type) {

		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(singleton(type));
		mappingContext.initialize();

		return mappingContext;
	}
}