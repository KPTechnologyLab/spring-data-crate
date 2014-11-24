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
import java.util.Map;

/**
 * {@link Column} holds definition for column. Generates crate specific ddl for column.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class Column {
	
	private String name;
	private String crateType;
	private String crateElementType;
	
	private Class<?> rawType;
	private Class<?> elementRawType;
	
	private boolean primaryKey;
	
	private List<Column> subColumns;
	
	public Column(String name, Class<?> rawType) {
		this(name, rawType, null);
	}
	
	public Column(String name, Class<?> rawType, Class<?> elementRawType) {
		
		hasText(name);
		notNull(rawType);
		
		this.name = name;
		this.rawType = rawType;
		setCrateType(getCrateTypeFor(rawType));
		
		if(elementRawType != null) {
			this.elementRawType = elementRawType;
			setElementCrateType(getCrateTypeFor(elementRawType));
		}
	}
	
	public String getName() {
		return name;
	}

	public String getCrateType() {
		return crateType;
	}

	private void setCrateType(String type) {
		hasText(type);
		this.crateType = type;
	}
	
	public String getElementCrateType() {
		return crateElementType;
	}

	private void setElementCrateType(String elementType) {
		hasText(elementType);
		this.crateElementType = elementType;
	}

	public Class<?> getElementRawType() {
		return elementRawType;
	}

	public Class<?> getRawType() {
		return rawType;
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
		return ARRAY.equalsIgnoreCase(crateType);
	}
	
	public boolean isPrimitiveArrayColumn() {
		return (isArrayColumn() && isPrimitiveElementType(crateElementType));
	}
	
	public boolean isObjectArrayColumn() {
		return (isArrayColumn() && !isPrimitiveElementType(crateElementType));
	}
	
	public boolean isObjectColumn() {
		return (OBJECT.equalsIgnoreCase(crateType) && !isMapColumn());
	}
	
	public boolean isMapColumn() {
		return Map.class.isAssignableFrom(rawType);
	}
	
	public boolean isPrimitiveElementType(String elementType) {
		return !OBJECT.equalsIgnoreCase(elementType);
	}
	
	@Override
	public String toString() {
		return "name=".concat(name)
					  .concat(", ")
					  .concat("crateType=")
					  .concat(crateType)
					  .concat(", ")
					  .concat("crateElementType=")
					  .concat(crateElementType == null ? "\"\"" : crateElementType)
					  .concat(", ")
					  .concat("subColumns=")
					  .concat(getSubColumns().toString());
	}
}