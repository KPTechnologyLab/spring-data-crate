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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.InvalidCrateApiUsageException;
import org.springframework.data.crate.core.CyclicReferenceException;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.schema.EntityColumnMapperTest.*;

import java.util.*;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.springframework.data.crate.core.mapping.CrateDataType.*;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */

@RunWith(Suite.class)
@SuiteClasses({PrimitivesColumnMappingTest.class, CollectionTypeColumnMappingTest.class,
        MapTypeColumnMappingTest.class, EntityTypeColumnMappingTest.class,
        PrimaryKeyColumnMappingTest.class})
public class EntityColumnMapperTest {

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    public static class PrimitivesColumnMappingTest {

        @Test
        public void shouldCreateStringColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithString.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));
            assertThat(columns.iterator().next().getCrateType(), is(STRING));
        }

        @Test
        public void shouldCreateShortColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithShort.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(SHORT));
            }
        }

        @Test
        public void shouldCreateByteColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithByte.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(BYTE));
            }
        }

        @Test
        public void shouldCreateIntegerColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithInt.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(INTEGER));
            }
        }

        @Test
        public void shouldCreateLongColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithLong.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(LONG));
            }
        }

        @Test
        public void shouldCreateFloatColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithFloat.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(FLOAT));
            }
        }

        @Test
        public void shouldCreateDoubleColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithDouble.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(DOUBLE));
            }
        }

        @Test
        public void shouldCreateBooleanColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithBoolean.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(BOOLEAN));
            }
        }

        @Test
        public void shouldCreateDateColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithDate.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));
            assertThat(columns.iterator().next().getCrateType(), is(TIMESTAMP));
        }

        @Test
        public void shouldCreateEnumColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithEnum.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));
            assertThat(columns.iterator().next().getCrateType(), is(STRING));
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

        static class EntityWithEnum {
            Locale locale;
        }
    }

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    public static class CollectionTypeColumnMappingTest {

        @Test
        public void shouldCreateStringArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithStringArray.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(STRING));
            }
        }

        @Test
        public void shouldCreateShortArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithShortCollection.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(SHORT));
            }
        }

        @Test
        public void shouldCreateIntegerArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithIntegerArray.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(INTEGER));
            }
        }

        @Test
        public void shouldCreateLongArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithLongCollection.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(LONG));
            }
        }

        @Test
        public void shouldCreateFloatArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithFloatCollection.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(FLOAT));
            }
        }

        @Test
        public void shouldCreateCollectionOfMapArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithSimpleKeyMapCollection.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(OBJECT));
                assertThat(column.getSubColumns().isEmpty(), is(true));
            }
        }

        @Test
        public void shouldCreateArrayOfMapArrayColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithSimpleKeyMapArray.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(OBJECT));
                assertThat(column.getSubColumns().isEmpty(), is(true));
            }
        }

        @Test
        public void shouldCreateArrayColumnsOfSameClassType() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithEntityCollection.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(ARRAY));
                assertThat(column.getElementCrateType(), is(OBJECT));
                assertThat(column.getSubColumns().size(), is(1));
                assertThat(column.getSubColumns().get(0).getCrateType(), is(ARRAY));
                assertThat(column.getSubColumns().get(0).getElementCrateType(), is(STRING));
            }
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateCollectionOfMapArrayColumn() {

            initMappingContextAndGetColumns(EntityWithComplexKeyMapCollection.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreatearrayOfMapArrayColumn() {

            initMappingContextAndGetColumns(EntityWithComplexKeyMapArray.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateDefinitionForNestedCollectionTypes() {

            initMappingContextAndGetColumns(EntityWithCollectionOfArray.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateDefinitionForNestedArrayTypes() {

            initMappingContextAndGetColumns(EntityWithArrayOfCollection.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
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

        static class EntityWithArrayOfArray {
            Set<String>[][] array;
        }

        static class EntityWithSimpleKeyMapCollection {
            List<Map<String, String>> maps;
        }

        static class EntityWithComplexKeyMapCollection {
            List<Map<EntityWithSimpleKeyMapCollection, String>> maps;
        }

        static class EntityWithSimpleKeyMapArray {
            Map<String, String>[] maps;
        }

        static class EntityWithComplexKeyMapArray {
            Map<EntityWithStringArray, String>[] maps;
        }

        static class EntityWithEntityCollection {
            List<EntityWithStringArray> entityList;
            EntityWithStringArray[] entityArray;
        }
    }

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    public static class MapTypeColumnMappingTest {

        @Test
        public void shouldCreateStringMapColumn() {

            List<Column> columns = initMappingContextAndGetColumns(EntityWithStringMap.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));

            for (Column column : columns) {
                assertThat(column.getCrateType(), is(OBJECT));
                assertThat(column.isMapColumn(), is(true));
                assertThat(column.isObjectColumn(), is(false));
            }
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateColumnWithComplexMapKey() {
            initMappingContextAndGetColumns(EntityWithComplexKeyMap.class);
        }

        static class EntityWithStringMap {
            Map<String, String> map;
        }

        static class EntityWithComplexKeyMap {
            Map<EntityWithStringMap, String> map;
        }
    }

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    public static class EntityTypeColumnMappingTest {

        @Test
        public void shouldCreateObjectColumn() {

            List<Column> columns = initMappingContextAndGetColumns(LevelOneEntity.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(2));

            // level 1
            assertThat(columns.get(0).getCrateType(), is(STRING));
            assertThat(columns.get(1).getCrateType(), is(OBJECT));
            assertThat(columns.get(1).isObjectColumn(), is(true));
            assertThat(columns.get(1).isMapColumn(), is(false));

            // level 2
            assertThat(columns.get(1).getSubColumns().size(), is(2));
            assertThat(columns.get(1).getSubColumns().get(0).getCrateType(), is(STRING));
            assertThat(columns.get(1).getSubColumns().get(1).getCrateType(), is(ARRAY));
            assertThat(columns.get(1).getSubColumns().get(1).getElementCrateType(), is(OBJECT));

            // level 3
            assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().size(), is(2));
            assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().get(0).getCrateType(), is(STRING));
            assertThat(columns.get(1).getSubColumns().get(1).getSubColumns().get(1).getCrateType(), is(OBJECT));
        }

        @Test(expected = CyclicReferenceException.class)
        public void shouldDetectCycleForSelfReferencing() {
            initMappingContextAndGetColumns(SelfReferencingEntity.class);
        }

        @Test(expected = CyclicReferenceException.class)
        public void shouldDetectCycleForNestedClassViaCollectionReferencing() {
            initMappingContextAndGetColumns(LevelOneCycle.class);
        }

        @Test(expected = CyclicReferenceException.class)
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

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    public static class PrimaryKeyColumnMappingTest {

        @Test
        public void shouldCreateObjectColumnAsPrimaryKey() {

            List<Column> columns = initMappingContextAndGetColumns(SimpleEntity.class);

            assertThat(columns, is(notNullValue()));
            assertThat(columns.size(), is(1));
            assertTrue(columns.iterator().next().isPrimaryKey());
            assertEquals(columns.iterator().next().getSubColumns().size(), 8);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateObjectColumnWithArrayAsPrimaryKey() {
            initMappingContextAndGetColumns(EntityWithArrayPK.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateObjectColumnWithCollectionAsPrimaryKey() {
            initMappingContextAndGetColumns(EntityWithCollectionPK.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateObjectColumnWithMapAsPrimaryKey() {
            initMappingContextAndGetColumns(EntityWithMapPK.class);
        }

        @Test(expected = InvalidCrateApiUsageException.class)
        public void shouldNotCreateObjectColumnWithObjectAsPrimaryKey() {
            initMappingContextAndGetColumns(EntityWithComplexPK.class);
        }

        static class SimpleEntity {
            @Id
            SimplePrimaryKey pk;
        }

        static class EntityWithArrayPK {
            @Id
            PrimaryKeyWithArray pk;
        }

        static class EntityWithCollectionPK {
            @Id
            PrimaryKeyWithCollection pk;
        }

        static class EntityWithMapPK {
            @Id
            PrimaryKeyWithMap pk;
        }

        static class EntityWithComplexPK {
            @Id
            ComplexPrimaryKey pk;
        }

        static class SimplePrimaryKey {

            boolean boolField;
            byte byteField;
            short shortField;
            int intField;
            long longField;
            float floatField;
            double doubleField;
            String stringField;
        }

        static class PrimaryKeyWithArray {
            boolean[] boolArray;
        }

        static class PrimaryKeyWithCollection {
            List<String> stringCollection;
        }

        static class PrimaryKeyWithMap {
            Map<String, Integer> map;
        }

        static class ComplexPrimaryKey {
            LevelOne levelOne;
        }

        static class LevelOne {
            String levelOneString;
        }
    }

    private static List<Column> initMappingContextAndGetColumns(Class<?> type) {
        CrateMappingContext mappingContext = prepareMappingContext(type);
        return new EntityColumnMapper(mappingContext).toColumns(mappingContext.getPersistentEntity(type));
    }

    private static CrateMappingContext prepareMappingContext(Class<?> type) {

        CrateMappingContext mappingContext = new CrateMappingContext();
        mappingContext.setInitialEntitySet(singleton(type));
        mappingContext.initialize();

        return mappingContext;
    }
}
