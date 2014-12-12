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
package org.springframework.data.crate.core.convert;

import static org.springframework.data.crate.core.convert.CrateTypeMapper.DEFAULT_TYPE_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Locale.CANADA;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.crate.core.mapping.CrateArray;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.mapping.model.MappingException;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MappingCrateConverterTest {
	
	private CrateMappingContext mappingContext;
	private MappingCrateConverter converter;
	
	@Mock 
	private ApplicationContext applicationContext;
	
	@Before
	public void setUp() throws Exception {
		mappingContext = new CrateMappingContext();
		mappingContext.afterPropertiesSet();		
		
		converter = new MappingCrateConverter(mappingContext);
		converter.setApplicationContext(applicationContext);
		converter.afterPropertiesSet();
	}
	
	@Test
	public void shouldNotWriteWhenSourceIsNull() {
		
		CrateDocument document = new CrateDocument(); 
		converter.write(null, document);
		
		assertThat(document.isEmpty(), is(true));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotWriteForRootAsCollectionType() {
		converter.write(asList("STRING"), new CrateDocument());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotWriteForNestedCollection() {
		
		Set<List<String>> nested = new HashSet<List<String>>(1);
		nested.add(asList("STRING"));
		
		NestedCollections entity = new NestedCollections();
		entity.nested = nested;
		
		converter.write(entity, new CrateDocument());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotWriteForNestedArray() {
		
		String[][] nested = new String[1][1];
		nested[0] = new String[]{"STRING"};
		
		NestedArrays entity = new NestedArrays();
		entity.nested = nested;
		
		converter.write(entity, new CrateDocument());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotWriteForNestedCollectionTypeId() {
		
		Set<List<String>> nested = new HashSet<List<String>>(1);
		nested.add(asList("STRING"));
		
		NestedCollectionId entity = new NestedCollectionId();
		entity.nested = nested;
		
		converter.write(entity, new CrateDocument());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotConvertForNestedCollectionTypeId() {
		
		Set<List<String>> nested = new HashSet<List<String>>(1);
		nested.add(asList("STRING"));
		
		NestedCollectionId entity = new NestedCollectionId();
		entity.nested = nested;
		
		converter.convertToCrateType(entity, null);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotWriteForComplexNestedCollectionTypeId() {
		
		Set<List<String>> nested = new HashSet<List<String>>(1);
		nested.add(asList("STRING"));
		
		NestedCollections nestedEntity = new NestedCollections();
		nestedEntity.nested = nested;
		
		ComplexNestedCollectionTypeId entity = new ComplexNestedCollectionTypeId();
		entity.nestedId = nestedEntity;
		
		converter.write(entity, new CrateDocument());
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotConvertForComplexNestedCollectionTypeId() {
		
		Set<List<String>> nested = new HashSet<List<String>>(1);
		nested.add(asList("STRING"));
		
		NestedCollections nestedEntity = new NestedCollections();
		nestedEntity.nested = nested;
		
		ComplexNestedCollectionTypeId entity = new ComplexNestedCollectionTypeId();
		entity.nestedId = nestedEntity;
		
		converter.convertToCrateType(entity, null);
	}
	
	@Test(expected=MappingException.class)
	public void shouldNotWriteForMapWithComplexKeys() {
		
		String[][] nested = new String[1][1];
		nested[0] = new String[]{"STRING"};
		
		NestedArrays compleKey = new NestedArrays();
		compleKey.nested = nested;
		
		Map<NestedArrays, String> map = new HashMap<MappingCrateConverterTest.NestedArrays, String>();
		map.put(compleKey, "ComplexKey");
		
		MapWithComplexKey entity = new MapWithComplexKey();
		entity.map = map;
		
		converter.write(entity, new CrateDocument());
	}
	
	@Test
	public void shouldWritePrimitives() {
		
		Date date = new Date();
		
		SimpleTypesEntity entity = new SimpleTypesEntity();
		entity.string = "STRING";
		entity.date = date;
		entity.bool = true;
		entity.locale = CANADA;
		
		Map<String, Object> expected = new HashMap<String, Object>();
	    expected.put(DEFAULT_TYPE_KEY, entity.getClass().getName());
	    expected.put("string", "STRING");
	    expected.put("integer", 0);
	    expected.put("date", date.getTime());
	    expected.put("bool", true);
	    expected.put("locale", CANADA.toString());
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		for(Entry<String, Object> entry : expected.entrySet()) {
			assertThat(document, hasEntry(entry.getKey(), entry.getValue()));
		}
	}
	
	@Test
	public void shouldReadPrimitives() {
		
		Date date = new Date();
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, SimpleTypesEntity.class.getName());
		document.put("string", "STRING");
		document.put("integer", 0);
		document.put("date", date);
		document.put("bool", true);
		document.put("locale", CANADA);
		
		SimpleTypesEntity entity = converter.read(SimpleTypesEntity.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.string, is("STRING"));
		assertThat(entity.integer, is(0));
		assertThat(entity.date, is(date));
		assertThat(entity.bool, is(true));
		assertThat(entity.locale, is(CANADA));
	}
	
	@Test
	public void shouldWriteCollectionOfPrimitive() {
		
		List<String> strings = asList("STRINGS");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, PrimitiveCollection.class.getName());
		expected.put("strings", strings);
		
		PrimitiveCollection entity = new PrimitiveCollection();
		entity.strings = strings;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("strings"));
		assertThat(document.get("strings"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("strings")).size(), is(1));
		assertThat(((CrateArray)document.get("strings")).get(0).toString(), is("STRINGS"));
	}
	
	@Test
	public void shouldReadCollectionOfPrimitive() {
		
		CrateArray strings = new CrateArray("STRINGS");
		
		CrateDocument document = new CrateDocument();
		document.put("strings", strings);
		
		PrimitiveCollection entity = converter.read(PrimitiveCollection.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.strings, is(notNullValue()));
		assertThat(entity.strings.size(), is(1));
		assertThat(entity.strings.get(0), is("STRINGS"));
	}
	
	@Test
	public void shouldWriteCollectionOfMaps() {
		
		CollectionOfMaps entity = new CollectionOfMaps();
		entity.maps = singletonList(singletonMap("STRING", 1));
		
		CrateDocument map = new CrateDocument("STRING", 1);
		CrateArray collectionDocument = new CrateArray(map);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, CollectionOfMaps.class.getName());
		expected.put("maps", collectionDocument);
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(expected.equals(document), is(true));
	}
	
	@Test
	public void shouldReadCollectionOfMaps() {
		
		CrateDocument map = new CrateDocument("STRING", 1);
		CrateArray collectionDocument = new CrateArray(map);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, CollectionOfMaps.class.getName());
		document.put("maps", collectionDocument);
		
		CollectionOfMaps entity = converter.read(CollectionOfMaps.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.maps, is(notNullValue()));
		assertThat(entity.maps.size(), is(1));
		assertThat(entity.maps.iterator().next(), is(notNullValue()));
		assertThat(entity.maps.iterator().next(), hasEntry("STRING", 1));
	}
	
	@Test
	public void shouldWriteEmptyCollection() {
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, PrimitiveCollection.class.getName());
		expected.put("strings", emptyList());
		
		PrimitiveCollection entity = new PrimitiveCollection();
		entity.strings = emptyList();
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("strings"));
		assertThat(document.get("strings"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("strings")), is(notNullValue()));
		assertThat(((CrateArray)document.get("strings")).isEmpty(), is(true));
	}
	
	@Test
	public void shouldReadEmptyCollection() {
		
		CrateDocument document = new CrateDocument();
		document.put("strings", new CrateArray());
		
		PrimitiveCollection entity = converter.read(PrimitiveCollection.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.strings, is(notNullValue()));
		assertThat(entity.strings.isEmpty(), is(true));
	}
	
	@Test
	public void shouldWriteNullValueForCollection() {
		
		List<String> strings = asList("STRINGS", null);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, PrimitiveCollection.class.getName());
		expected.put("strings", strings);
		
		PrimitiveCollection entity = new PrimitiveCollection();
		entity.strings = strings;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("strings"));
		assertThat(document.get("strings"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("strings")).size(), is(2));
		assertThat(((CrateArray)document.get("strings")).get(0).toString(), is("STRINGS"));
		assertThat(((CrateArray)document.get("strings")).get(1), is(nullValue()));
	}
	
	@Test
	public void shouldReadNullValueForCollection() {
		
		List<String> strings = asList("STRINGS", null);
		
		CrateDocument document = new CrateDocument();
		document.put("strings", strings);
		
		PrimitiveCollection entity = converter.read(PrimitiveCollection.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.strings, is(notNullValue()));
		assertThat(entity.strings.size(), is(2));
		assertThat(entity.strings.get(0), is("STRINGS"));
		assertThat(entity.strings.get(1), is(nullValue()));
	}
	
	@Test
	public void shouldWriteArrayOfPrimitive() {
		
		boolean[] array = new boolean[]{true};
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, PrimitiveArray.class.getName());
		expected.put("booleans", array);
		
		PrimitiveArray entity = new PrimitiveArray();
		entity.booleans = array;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)PrimitiveArray.class.getName()));
		assertThat(document, hasKey("booleans"));
		assertThat(document.get("booleans"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("booleans")).size(), is(1));
		assertThat(((CrateArray)document.get("booleans")).get(0).toString(), is("true"));
	}
	
	@Test
	public void shouldReadArrayOfPrimitive() {
		
		CrateArray array = new CrateArray(true);
		
		CrateDocument document = new CrateDocument();
		document.put("booleans", array);
		
		PrimitiveArray entity = converter.read(PrimitiveArray.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.booleans, is(notNullValue()));
		assertThat(entity.booleans[0], is(true));
	}
	
	@Test
	public void shouldWriteArrayOfPrimitiveWrapper() {
		
		Integer[] array = new Integer[]{new Integer(1)};
		
		CrateArray crateArray = new CrateArray(new Integer(1));
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, PrimitiveWrapperArray.class.getName());
		expected.put("integers", crateArray);
		
		PrimitiveWrapperArray entity = new PrimitiveWrapperArray();
		entity.integers = array;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)PrimitiveWrapperArray.class.getName()));
		assertThat(document, hasKey("integers"));
		assertThat(document.get("integers"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("integers")).size(), is(1));
		assertThat(((CrateArray)document.get("integers")).get(0).toString(), is("1"));
	}
	
	@Test
	public void shouldReadArrayOfPrimitiveWrapper() {
		
		CrateArray array = new CrateArray(new Integer(1));
		
		CrateDocument document = new CrateDocument();
		document.put("integers", array);
		
		PrimitiveWrapperArray entity = converter.read(PrimitiveWrapperArray.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.integers, is(notNullValue()));
		assertThat(entity.integers[0], is(1));
	}
	
	@Test
	public void shouldWriteArrayOfString() {
		
		String[] array = new String[]{"C","R","A","T","E"};
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, StringArray.class.getName());
		expected.put("strings", array);
		
		StringArray entity = new StringArray();
		entity.strings = array;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)StringArray.class.getName()));
		assertThat(document, hasKey("strings"));
		assertThat(document.get("strings"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("strings")).size(), is(5));
		assertThat(((CrateArray)document.get("strings")).toArray(), is((Object)array));
	}
	
	@Test
	public void shouldReadArrayOfString() {
		
		CrateArray array = new CrateArray();
		array.addAll(asList("C","R","A","T","E"));
		
		CrateDocument document = new CrateDocument();
		document.put("strings", array);
		
		StringArray entity = converter.read(StringArray.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.strings, is(notNullValue()));
		assertThat(entity.strings, is(array.toArray()));
	}
	
	@Test
	public void shouldWriteMapOfPrimitive() {
		
		Map<Double, String> map = new HashMap<Double, String>();
		map.put(1.0, "STRING");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, MapOfPrimitive.class.getName());
		expected.put("map", map);
		
		MapOfPrimitive entity = new MapOfPrimitive();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("map"));
		assertThat(document.get("map"), is(instanceOf(CrateDocument.class)));
		assertThat(((CrateDocument)document.get("map")), hasEntry("1.0", (Object)"STRING"));
	}
	
	@Test
	public void shouldReadMapOfPrimitive() {
		
		CrateDocument map = new CrateDocument();
		map.put("1.0", "STRING");
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, MapOfPrimitive.class.getName());
		document.put("map", map);
		
		MapOfPrimitive entity = converter.read(MapOfPrimitive.class, document);

		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map, hasEntry(1.0, "STRING"));
	}
	
	@Test
	public void shouldWriteMapOfPrimitiveArray() {
		
		int[] array = new int[]{1};
		
		Map<String, int[]> map = new HashMap<String, int[]>();
		map.put("STRING", array);
		
		CrateArray expectedArray = new CrateArray(1); 
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, MapOfPrimitiveArray.class.getName());
		expected.put("map", expectedArray);
		
		MapOfPrimitiveArray entity = new MapOfPrimitiveArray();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("map"));
		assertThat(document.get("map"), is(instanceOf(CrateDocument.class)));
		assertThat(((CrateDocument)document.get("map")), hasEntry("STRING", (Object)expectedArray));
	}
	
	@Test
	public void shouldReadMapOfPrimitiveArray() {
		
		int[] array = new int[]{1};
		
		CrateArray crateArray = new CrateArray(1);
		CrateDocument map = new CrateDocument();
		map.put("STRING", crateArray);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, MapOfPrimitiveArray.class.getName());
		document.put("map", map);
		
		MapOfPrimitiveArray entity = converter.read(MapOfPrimitiveArray.class, document);

		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map, hasEntry("STRING", (Object)array));
	}
	
	@Test
	public void shouldWriteMapOfPrimitiveWrapperArray() {
		
		Boolean[] array = new Boolean[]{true, false};
		
		Map<String, Boolean[]> map = new HashMap<String, Boolean[]>();
		map.put("STRING", array);

		CrateArray expectedArray = new CrateArray();
		expectedArray.addAll(asList(array));
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, MapOfPrimitiveWrapperArray.class.getName());
		expected.put("map", map);
		
		MapOfPrimitiveWrapperArray entity = new MapOfPrimitiveWrapperArray();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("map"));
		assertThat(document.get("map"), is(instanceOf(CrateDocument.class)));
		assertThat(((CrateDocument)document.get("map")), hasEntry("STRING", (Object)expectedArray));
	}
	
	@Test
	public void shouldReadMapOfPrimitiveWrapperArray() {
		
		Boolean[] array = new Boolean[]{true, false};
		
		CrateArray crateArray = new CrateArray();
		crateArray.addAll(asList(array));
		
		CrateDocument map = new CrateDocument();
		map.put("STRING", crateArray);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, MapOfPrimitiveWrapperArray.class.getName());
		document.put("map", map);
		
		MapOfPrimitiveWrapperArray entity = converter.read(MapOfPrimitiveWrapperArray.class, document);

		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map, hasEntry("STRING", (Object)array));
	}
	
	@Test
	public void shouldWriteMapOfObject() {
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument mapDocument = new CrateDocument("country", countryDocument);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, MapOfObject.class.getName());
		expected.put("map", mapDocument);
		
		Map<String, Country> map = new HashMap<String, Country>();
		map.put("country", new Country("aCountry", asList(new Language("aLanguage"))));
		
		MapOfObject entity = new MapOfObject();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(expected.equals(document), is(true));
	}
	
	@Test
	public void shouldReadMapOfObject() {
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument mapDocument = new CrateDocument("country", countryDocument);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, MapOfObject.class.getName());
		document.put("map", mapDocument);
		
		MapOfObject entity = converter.read(MapOfObject.class, document);

		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map.isEmpty(), is(false));
		assertThat(entity.map, hasKey("country"));
		assertThat(entity.map.get("country"), is(notNullValue()));
		assertThat(entity.map.get("country").name, is("aCountry"));
		assertThat(entity.map.get("country").languages, is(notNullValue()));
		assertThat(entity.map.get("country").languages.isEmpty(), is(false));
		assertThat(entity.map.get("country").languages.get(0), is(notNullValue()));
		assertThat(entity.map.get("country").languages.get(0).name, is("aLanguage"));
	}
	
	@Test
	public void shouldWriteNullValueForMap() {
		
		Map<Double, String> map = new HashMap<Double, String>();
		map.put(1.0, "STRING");
		map.put(2.0, null);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, MapOfPrimitive.class.getName());
		expected.put("map", map);
		
		MapOfPrimitive entity = new MapOfPrimitive();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)entity.getClass().getName()));
		assertThat(document, hasKey("map"));
		assertThat(document.get("map"), is(instanceOf(CrateDocument.class)));
		assertThat(((CrateDocument)document.get("map")), hasEntry("1.0", (Object)"STRING"));
		assertThat(((CrateDocument)document.get("map")), hasEntry("2.0", null));
	}
	
	@Test
	public void shouldReadNullValueForMap() {
		
		Map<Double, String> map = new HashMap<Double, String>();
		map.put(1.0, "STRING");
		map.put(2.0, null);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, MapOfPrimitive.class.getName());
		document.put("map", map);
		
		MapOfPrimitive entity = converter.read(MapOfPrimitive.class, document);

		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map, hasEntry(1.0, "STRING"));
		assertThat(entity.map, hasEntry(2.0, null));
	}
	
	@Test
	public void shouldWriteNestedMaps() {
		
		Map<Integer, List<Boolean>> nested = new HashMap<Integer, List<Boolean>>();
		nested.put(1, asList(true, false));
		
		Map<String, Map<Integer, List<Boolean>>> map = new HashMap<String, Map<Integer,List<Boolean>>>();
		map.put("Key", nested);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, NestedMaps.class.getName());
		expected.put("map", new CrateDocument("Key", new CrateDocument("1", asList(true, false))));
		
		NestedMaps entity = new NestedMaps();
		entity.map = map;
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(expected.equals(document), is(true));
	}
	
	@Test
	public void shouldReadNestedMaps() {
		
		CrateDocument document = new CrateDocument("map", new CrateDocument("Key", new CrateDocument("1", asList(true, false))));
		
		NestedMaps entity = converter.read(NestedMaps.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.map, is(notNullValue()));
		assertThat(entity.map, hasKey("Key"));
		assertThat(entity.map.get("Key"), is(notNullValue()));
		assertThat(entity.map.get("Key").size(), is(1));
		assertThat(entity.map.get("Key").get(1), hasItems(true, false));
	}
	
	@Test
	public void shouldWriteComplexModel() {
		
		Country country = new Country("aCountry", asList(new Language("aLanguage")));
		Address address = new Address();
		address.country = country;
		address.city = "aCity";
		address.street = "aStreet";
		
		Person person = new Person();
		person.name = "aName";
		person.address = address;
		person.emails = new HashSet<MappingCrateConverterTest.Email>(asList(new Email("email@test.com")));
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateDocument emailDocument = new CrateDocument("email", "email@test.com");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		CrateArray emailsArray = new CrateArray(emailDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument addressDocument = new CrateDocument();
		addressDocument.put("country", countryDocument);
		addressDocument.put("city", "aCity");
		addressDocument.put("street", "aStreet");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, Person.class.getName());
		expected.put("name", "aName");
		expected.put("address", addressDocument);
		expected.put("emails", emailsArray);
		
		CrateDocument document = new CrateDocument();
		
		converter.write(person, document);

		assertThat(expected.equals(document), is(true));
	}
	
	@Test
	public void shouldReadComplexModel() {
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateDocument emailDocument = new CrateDocument("email", "email@test.com");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		CrateArray emailsArray = new CrateArray(emailDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument addressDocument = new CrateDocument();
		addressDocument.put("country", countryDocument);
		addressDocument.put("city", "aCity");
		addressDocument.put("street", "aStreet");
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, Person.class.getName());
		document.put("name", "aName");
		document.put("address", addressDocument);
		document.put("emails", emailsArray);
		
		Person entity = converter.read(Person.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.name, is("aName"));
		assertThat(entity.address, is(notNullValue()));
		assertThat(entity.address.country, is(notNullValue()));
		assertThat(entity.address.country.name, is("aCountry"));
		assertThat(entity.address.country.languages, is(notNullValue()));
		assertThat(entity.address.country.languages.size(), is(1));
		assertThat(entity.address.country.languages.get(0), is(notNullValue()));
		assertThat(entity.address.country.languages.get(0).name, is("aLanguage"));
		assertThat(entity.address.city, is("aCity"));
		assertThat(entity.address.street, is("aStreet"));
		assertThat(entity.emails, is(notNullValue()));
		assertThat(entity.emails.size(), is(1));
		assertThat(entity.emails.iterator().next(), is(notNullValue()));
		assertThat(entity.emails.iterator().next().email, is("email@test.com"));
	}
	
	@Test
	public void shouldWriteGenericTypeCorrectly() {

		GenericType<Language> genericType = new GenericType<Language>();
		genericType.content = new Language("aLanguage");

		CrateDocument document = new CrateDocument();
		converter.write(genericType, document);

		assertThat(document, hasEntry(DEFAULT_TYPE_KEY, (Object)GenericType.class.getName()));
		assertThat(document, hasKey("content"));
		
		CrateDocument languageDocument = (CrateDocument) document.get("content");
		assertThat(languageDocument, is(notNullValue()));
		assertThat(languageDocument, hasEntry(DEFAULT_TYPE_KEY, (Object)Language.class.getName()));
		assertThat(languageDocument, hasEntry("name", (Object)"aLanguage"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReadGenericTypeCorrectly() {

		CrateDocument languageDocument = new CrateDocument(DEFAULT_TYPE_KEY, Language.class.getName());
		languageDocument.put("name", "aLanguage");
		
		CrateDocument document = new CrateDocument(DEFAULT_TYPE_KEY, GenericType.class.getName());
		document.put("content", languageDocument);
		
		GenericType<Language> genericType =  converter.read(GenericType.class, document);

		assertThat(genericType, is(notNullValue()));
		assertThat(genericType.content, is(notNullValue()));
		assertThat(genericType.content.name, is("aLanguage"));
	}
	
	@Test
	public void shouldWriteMapAsGenericFieldCorrectly() {

		Map<String, GenericClass<String>> map = new HashMap<String, GenericClass<String>>();
		map.put("test", new GenericClass<String>("testValue"));

		GenericClass<Map<String, GenericClass<String>>> genericClass = new GenericClass<Map<String, GenericClass<String>>>(map);
		CrateDocument document = new CrateDocument();

		converter.write(genericClass, document);

		assertThat((String) document.get(DEFAULT_TYPE_KEY), is(GenericClass.class.getName()));
		assertThat((String) document.get("valueType"), is(HashMap.class.getName()));

		CrateDocument object = (CrateDocument) document.get("value");
		assertThat(object, is(notNullValue()));

		CrateDocument inner = (CrateDocument) object.get("test");
		assertThat(inner, is(notNullValue()));
		assertThat((String) inner.get(DEFAULT_TYPE_KEY), is(GenericClass.class.getName()));
		assertThat((String) inner.get("valueType"), is(String.class.getName()));
		assertThat((String) inner.get("value"), is("testValue"));
	}
	
	@Test
	public void shouldWriteWithSimpleId() {
		
		SimpleStringId stringId = new SimpleStringId();
		stringId.stringId = "CRATE";
		
		CrateDocument stringIdDocument = new CrateDocument();
		
		converter.write(stringId, stringIdDocument);
		
		assertThat(stringIdDocument, hasEntry(DEFAULT_TYPE_KEY, (Object)SimpleStringId.class.getName()));
		assertThat(stringIdDocument, hasEntry("stringId", (Object)"CRATE"));
		
		SimpleIntId intId = new SimpleIntId();
		intId.intId = 2620;
		
		CrateDocument intIdDocument = new CrateDocument();
		
		converter.write(intId, intIdDocument);
		
		assertThat(intIdDocument, hasEntry(DEFAULT_TYPE_KEY, (Object)SimpleIntId.class.getName()));
		assertThat(intIdDocument, hasEntry("intId", (Object)2620));
		
		SimpleLongId longId = new SimpleLongId();
		longId.longId = 4732L;
		
		CrateDocument longIdDocument = new CrateDocument();
		
		converter.write(longId, longIdDocument);
		
		assertThat(longIdDocument, hasEntry(DEFAULT_TYPE_KEY, (Object)SimpleLongId.class.getName()));
		assertThat(longIdDocument, hasEntry("longId", (Object)4732L));
	}
	
	@Test
	public void shouldReadWithSimpleId() {
		
		SimpleStringId stringId = converter.read(SimpleStringId.class, new CrateDocument("stringId", "CRATE"));
		
		assertThat(stringId, is(notNullValue()));
		assertThat(stringId.stringId, is("CRATE"));
		
		SimpleIntId intId = converter.read(SimpleIntId.class, new CrateDocument("intId", 2620));
		
		assertThat(intId, is(notNullValue()));
		assertThat(intId.intId, is(2620));
		
		SimpleLongId longId = new SimpleLongId();
		longId.longId = 4732L;
		
		converter.read(SimpleLongId.class, new CrateDocument("longId", 4732L));
		
		assertThat(longId, is(notNullValue()));
		assertThat(longId.longId, is(4732L));
	}
	
	@Test
	public void shouldWriteWithComplexId() {
		
		Country country = new Country("aCountry", asList(new Language("aLanguage")));
		Address address = new Address();
		address.country = country;
		address.city = "aCity";
		address.street = "aStreet";
		
		Person person = new Person();
		person.name = "aName";
		person.address = address;
		person.emails = new HashSet<MappingCrateConverterTest.Email>(asList(new Email("email@test.com")));
		
		ComplexId entity = new ComplexId();
		entity.pk = person;
		entity.string = "STRING";
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateDocument emailDocument = new CrateDocument("email", "email@test.com");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		CrateArray emailsArray = new CrateArray(emailDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument addressDocument = new CrateDocument();
		addressDocument.put("country", countryDocument);
		addressDocument.put("city", "aCity");
		addressDocument.put("street", "aStreet");
		
		CrateDocument personDocument = new CrateDocument();
		personDocument.put("name", "aName");
		personDocument.put("address", addressDocument);
		personDocument.put("emails", emailsArray);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(DEFAULT_TYPE_KEY, ComplexId.class.getName());
		expected.put("string", "STRING");
		expected.put("pk", personDocument);
		
		CrateDocument document = new CrateDocument();
		
		converter.write(entity, document);
		
		assertThat(expected.equals(document), is(true));
	}
	
	@Test
	public void shouldReadWithComplexId() {
		
		CrateDocument languageDocument = new CrateDocument("name", "aLanguage");
		
		CrateDocument emailDocument = new CrateDocument("email", "email@test.com");
		
		CrateArray languagesArray = new CrateArray(languageDocument);
		CrateArray emailsArray = new CrateArray(emailDocument);
		
		CrateDocument countryDocument = new CrateDocument();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		CrateDocument addressDocument = new CrateDocument();
		addressDocument.put("country", countryDocument);
		addressDocument.put("city", "aCity");
		addressDocument.put("street", "aStreet");
		
		CrateDocument personDocument = new CrateDocument();
		personDocument.put("name", "aName");
		personDocument.put("address", addressDocument);
		personDocument.put("emails", emailsArray);
		
		CrateDocument document = new CrateDocument();
		document.put(DEFAULT_TYPE_KEY, ComplexId.class.getName());
		document.put("string", "STRING");
		document.put("pk", personDocument);
		
		ComplexId entity = converter.read(ComplexId.class, document);
		
		assertThat(entity, is(notNullValue()));
		assertThat(entity.string, is("STRING"));
		assertThat(entity.pk, is(notNullValue()));
		assertThat(entity.pk.name, is("aName"));
		assertThat(entity.pk.address, is(notNullValue()));
		assertThat(entity.pk.address.country, is(notNullValue()));
		assertThat(entity.pk.address.country.name, is("aCountry"));
		assertThat(entity.pk.address.country.languages, is(notNullValue()));
		assertThat(entity.pk.address.country.languages.size(), is(1));
		assertThat(entity.pk.address.country.languages.get(0), is(notNullValue()));
		assertThat(entity.pk.address.country.languages.get(0).name, is("aLanguage"));
		assertThat(entity.pk.address.city, is("aCity"));
		assertThat(entity.pk.address.street, is("aStreet"));
		assertThat(entity.pk.emails, is(notNullValue()));
		assertThat(entity.pk.emails.size(), is(1));
		assertThat(entity.pk.emails.iterator().next(), is(notNullValue()));
		assertThat(entity.pk.emails.iterator().next().email, is("email@test.com"));
		
	}
	
	static class NestedCollections {
		Set<List<String>> nested;
	}
	
	static class NestedArrays {
		String[][] nested;
	}
	
	static class NestedCollectionId {
		@Id
		Set<List<String>> nested;
	}
	
	static class ComplexNestedCollectionTypeId {
		@Id
		NestedCollections nestedId;
	}
	
	static class MapWithComplexKey {
		Map<NestedArrays, String> map;
	}
	
	static class SimpleTypesEntity {
		String string;
		int integer;
		Date date;
		Boolean bool;
		Locale locale;
	}
	
	static class PrimitiveCollection {
		List<String> strings;
	}
	
	static class PrimitiveArray {
		boolean[] booleans;
	}
	
	static class PrimitiveWrapperArray {
		Integer[] integers;
	}
	
	static class StringArray {
		String[] strings;
	}
	
	static class CollectionOfMaps {
		Collection<Map<String, Integer>> maps;
	}
	
	static class MapOfPrimitive {
		Map<Double, String> map;
	}
	
	static class MapOfPrimitiveArray {
		Map<String, int[]> map;
	}
	
	static class MapOfPrimitiveWrapperArray {
		Map<String, Boolean[]> map;
	}
	
	static class MapOfObject {
		Map<String, Country> map;
	}
	
	static class NestedMaps {
		Map<String, Map<Integer, List<Boolean>>> map;
	}
	
	static class Language {
		String name;
		
		public Language(String name) {
			this.name = name;
		}
	}
	
	static class Country {
		String name;
		List<Language> languages;

		public Country(String name) {
			this(name, null);
		}

		@PersistenceConstructor
		public Country(String name, List<Language> languages) {
			super();
			this.name = name;
			this.languages = languages;
		}
	}
	
	static class Address {
		Country country;
		String street;
		String city;
	}
	
	static class Email {
		String email;
		
		public Email(String email) {
			super();
			this.email = email;
		}
	}
	
	static class Person {
		String name;
		Address address;
		Set<Email> emails;
	}
	
	static class GenericClass<T> {

		String valueType;
		T value;

		public GenericClass(T value) {
			this.valueType = value.getClass().getName();
			this.value = value;
		}
	}
	
	static class GenericType<T> {
		T content;
	}
	
	static class ComplexId {
		@Id
		Person pk;
		String string;
	}
	
	static class SimpleStringId {
		@Id
		String stringId;
	}
	
	static class SimpleIntId {
		@Id
		int intId;
	}
	
	static class SimpleLongId {
		@Id		
		long longId;
	}
}