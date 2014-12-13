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

import static java.util.Arrays.asList;
import static java.util.Locale.CANADA;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import io.crate.types.ArrayType;
import io.crate.types.BooleanType;
import io.crate.types.DataType;
import io.crate.types.IntegerType;
import io.crate.types.ObjectType;
import io.crate.types.StringType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.CrateArray;
import org.springframework.data.crate.core.mapping.CrateDocument;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CrateDocumentConverterTest {

	private CrateDocumentConverter converter;
	
	@Test
	public void shouldCreateEmptyDocument() {
		
		String[] columns = {"string"};
		
		DataType<?>[] types = { StringType.INSTANCE };
		
		Object[][] rows = new Object[0][0];
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.isEmpty(), is(true));
		
		converter = new CrateDocumentConverter(columns, types, null);
		
		document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.isEmpty(), is(true));
	}

	@Test
	public void shouldMapSimpleTypes() {
		
		String[] columns = {"string", "integer", "bool", "locale"};
		
		DataType<?>[] types = { StringType.INSTANCE, IntegerType.INSTANCE, 
							    BooleanType.INSTANCE, StringType.INSTANCE };
		
		Object[][] rows = new Object[][]{new Object[]{"DOCUMENT", 1, true, CANADA}};
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("string", "DOCUMENT");
		expected.put("integer", 1);
		expected.put("bool", true);
		expected.put("locale", CANADA);
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.equals(expected), is(true));
	}
	
	@Test
	public void shouldMapSimpleCollectionTypes() {
		
		List<String> strings = asList("C", "R", "A", "T", "E");
		List<Integer> integers = asList(1, 2);
		
		String[] columns = {"strings", "integers"};
		
		DataType<?>[] types = { new ArrayType(StringType.INSTANCE), 
								new ArrayType(IntegerType.INSTANCE) };
		
		Object[][] rows = new Object[][]{new Object[]{strings, integers}};
		
		CrateArray stringsArray = new CrateArray();
		stringsArray.addAll(strings);
		
		CrateArray integersArray = new CrateArray();
		integersArray.addAll(integers);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("strings", stringsArray);
		expected.put("integers", integersArray);
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.equals(expected), is(true));
	}
	
	@Test
	public void shouldMapArrayTypes() {
		
		String[] strings = {"C", "R", "A", "T", "E"};
		
		String[] columns = {"strings"};
		
		DataType<?>[] types = { new ArrayType(StringType.INSTANCE) };
		
		Object[][] rows = new Object[][]{new Object[]{strings}};
		
		CrateArray stringsArray = new CrateArray();
		stringsArray.addAll(asList(strings));
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("strings", stringsArray);
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.equals(expected), is(true));
	}
	
	@Test
	public void shouldMapNestedDocument() {
		
		Map<String, Object> nested = new HashMap<String, Object>();
		nested.put("string", "STRING_FIELD");
		nested.put("integer", 1);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("nested", nested);
		
		String[] columns = {"nested"};
		
		DataType<?>[] types = { ObjectType.INSTANCE };
		
		Object[][] rows = new Object[][]{new Object[]{nested}};
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.equals(expected), is(true));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldMapObjectCollectionTypes() {
		
		Map<String, Object> object_1 = new HashMap<String, Object>();
		object_1.put("string_1", "STRING_FIELD_1");
		object_1.put("integer_1", 1);
		
		Map<String, Object> object_2 = new HashMap<String, Object>();
		object_2.put("strings", asList("C", "R", "A", "T", "E"));
		
		List<Map<String, Object>> objects = asList(object_1, object_2);
		
		CrateArray array = new CrateArray();
		array.addAll(objects);
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("objects", array);
		
		String[] columns = {"objects"};
		
		DataType<?>[] types = { new ArrayType(ObjectType.INSTANCE) };
		
		Object[][] rows = new Object[][]{new Object[]{objects}};
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.get("objects"), is(instanceOf(CrateArray.class)));
		assertThat(((CrateArray)document.get("objects")).size(), is(2));
		assertThat(((CrateArray)document.get("objects")), hasItems((Object)object_1, (Object)object_2));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldMapComplexType() {
		
		Map<String, Object> languageDocument = new HashMap<String, Object>();
		languageDocument.put("name", "aLanguage");
		
		Map<String, Object> emailDocument = new HashMap<String, Object>();
		emailDocument.put("email", "email@test.com");
		
		List<Map<String, Object>> languagesArray = asList(languageDocument);
		List<Map<String, Object>> emailsArray = asList(emailDocument);
		
		Map<String, Object> countryDocument = new HashMap<String, Object>();
		countryDocument.put("name", "aCountry");
		countryDocument.put("languages", languagesArray);
		
		Map<String, Object> addressDocument = new HashMap<String, Object>();
		addressDocument.put("country", countryDocument);
		addressDocument.put("city", "aCity");
		addressDocument.put("street", "aStreet");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("name", "aName");
		expected.put("address", addressDocument);
		expected.put("emails", emailsArray);
		
		String[] columns = {"name", "address", "emails"};
		
		DataType<?>[] types = { StringType.INSTANCE, ObjectType.INSTANCE, new ArrayType(ObjectType.INSTANCE) };
		
		Object[][] rows = new Object[][]{new Object[]{"aName", addressDocument, emailsArray}};
		
		converter = new CrateDocumentConverter(columns, types, rows);
		
		CrateDocument document = converter.toDocument();
		
		assertThat(document, is(notNullValue()));
		assertThat(document.size(), is(3));
		assertThat(document.equals(expected), is(true));
	}
}