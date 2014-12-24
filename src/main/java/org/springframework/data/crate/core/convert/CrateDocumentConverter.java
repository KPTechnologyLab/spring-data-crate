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
import static org.apache.commons.lang.ArrayUtils.getLength;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY_SUFFIX;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.util.ClassTypeInformation.from;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;
import io.crate.types.DataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.springframework.data.crate.core.mapping.CrateArray;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.util.TypeInformation;


/**
 * {@link CrateDocumentConverter} translates sql response payload (row) for a single entity type to {@link CrateDocument}
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CrateDocumentConverter {
	
	private final Logger logger = getLogger(getClass());
	
	private List<Row> rows;
	
	public CrateDocumentConverter(String[] columns, DataType<?>[] types, Object[] row) {
		notEmpty(columns);
		notEmpty(types);
		
		this.rows = isNotEmpty(row) ? initRows(columns, types, row) : Collections.<Row>emptyList();
	}
	
	public CrateDocument toDocument() {
		
		CrateDocument root = new CrateDocument();
		
		for(Row row : rows) {
			if(row.isObject()) {
				CrateDocument document = new CrateDocument();
				toCrateDocument(document, row.getPayload());
				root.put(row.getColumn(), document);
				logger.debug("pushed '{}' as CrateDocument to root CrateDocument", row.getColumn());
			}else if(row.isArray()) {
				CrateArray array = new CrateArray();
				toCrateArray(array, row.getPayload());
				root.put(row.getColumn(), array);
				logger.debug("pushed '{}' as CrateArray to root CrateDocument", row.getColumn());
			}else {
				root.put(row.getColumn(), row.getPayload());
				logger.debug("pushed '{}' as simple type '{}' to root CrateDocument",
						 	  new Object[]{row.getColumn(), row.getPayload().getClass().getName()});
			}
		}
		
		return root;
	}
	
	/**
	 * 
	 * @param root container for the converted payload
	 * @param payload value to be converted to {@link CrateDocument}
	 */
	@SuppressWarnings("unchecked")
	private void toCrateDocument(CrateDocument root, Object payload) {
		
		Map<String, Object> map = (Map<String, Object>)payload;
		
		for(Entry<String, Object> entry : map.entrySet()) {
			
			TypeInformation<?> type = getTypeInformation(entry.getValue().getClass());
			
			if(type.isMap()) {
				CrateDocument document = new CrateDocument();
				toCrateDocument(document, entry.getValue());
				logger.debug("converted '{}' to CrateDocument", entry.getKey());
				root.put(entry.getKey(), document);
			}else if(type.isCollectionLike()) {
				CrateArray array = new CrateArray();
				toCrateArray(array, entry.getValue());
				logger.debug("converted '{}' to CrateArray", entry.getKey());
				root.put(entry.getKey(), array);
			}else {
				// simple type
				root.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Nesting Array or Collection types is not supported by crate. It is safe to assume that the payload
	 * will contain either a Map or a primitive type. Map types will be converted to {@link CrateDocument}
	 * while simple types will be added without any conversion
	 * @param array {@link CrateArray} for adding either Map or Simple types
	 * @param payload containing either a Map or primitive type.
	 */
	@SuppressWarnings("unchecked")
	private void toCrateArray(CrateArray array, Object payload) {
		
		Collection<Object> objects = (Collection<Object>)(payload.getClass().isArray() ? asList((Object[])payload) : payload);
		
		for(Object object : objects) {
			
			TypeInformation<?> type = getTypeInformation(object.getClass());
			
			if(type.isMap()) {
				CrateDocument document = new CrateDocument();
				toCrateDocument(document, object);
				array.add(document);
			}else {
				array.add(object);
			}
		}
	}
	
	private List<Row> initRows(String[] columns, DataType<?>[] types, Object[] row) {
		
		List<Row> payload = new ArrayList<CrateDocumentConverter.Row>(getLength(rows));

		for (int index = 0; index < row.length; index++) {
			payload.add(new Row(columns[index], types[index], row[index]));
		}
		
		return payload;
	}
	
	private TypeInformation<?> getTypeInformation(Class<?> clazz) {
		return from(clazz);
	}
	
	private class Row {
		
		private String column;
		private String type;
		private Object payload;
		
		public Row(String column, DataType<?> type, Object payload) {
			
			hasText(column);
			notNull(type);
			
			this.column = column;
			this.type = type.getName();
			this.payload = payload;
		}

		public String getColumn() {
			return column;
		}

		public String getType() {
			return type;
		}

		public Object getPayload() {
			return payload;
		}
		
		public boolean isObject() {
			return OBJECT.equals(getType());
		}
		
		public boolean isArray() {
			return getType().endsWith(ARRAY_SUFFIX);
		}
		
		@Override
		public String toString() {
			return reflectionToString(this);
		}
	}
}