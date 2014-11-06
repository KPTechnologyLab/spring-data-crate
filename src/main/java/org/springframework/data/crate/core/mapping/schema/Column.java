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

import static java.util.Collections.emptyList;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.crate.core.mapping.CrateDataType.getCrateTypeFor;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.List;

/**
 * {@link Column} holds definition for column. Generates crate specific ddl for column.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
class Column {
	
	private String name;
	private String type;
	private String elementType;
	
	private Class<?> rawType;
	
	private boolean primaryKey;
	
	private List<Column> subColumns;
	
	public Column(String name, Class<?> rawType) {
		setName(name);
		setRawType(rawType);
		setType(getCrateTypeFor(rawType));
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		hasText(name);
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		hasText(type);
		this.type = type;
	}
	
	public String getElementType() {
		return elementType;
	}

	public void setElementType(Class<?> elementType) {
		notNull(elementType);
		this.elementType = getCrateTypeFor(elementType);
		hasText(this.elementType);
	}
	
	public Class<?> getRawType() {
		return rawType;
	}

	public void setRawType(Class<?> rawType) {
		notNull(rawType);
		this.rawType = rawType;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<Column> getSubColumns() {
		
		if(subColumns == null) {
			return emptyList();
		}
		
		return subColumns;
	}

	public void setSubColumns(List<Column> subColumns) {
		this.subColumns = subColumns;
	}

	public boolean isArrayColumn() {
		return ARRAY.equalsIgnoreCase(type);
	}
	
	public boolean isObjectColumn() {
		return OBJECT.equalsIgnoreCase(type);
	}
	
	public boolean isPrimitiveElementType(String elementType) {
		return !OBJECT.equalsIgnoreCase(elementType);
	}
}