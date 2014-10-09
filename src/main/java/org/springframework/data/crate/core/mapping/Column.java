package org.springframework.data.crate.core.mapping;

import static java.util.Collections.emptyList;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.util.Assert.hasText;

import java.util.List;

class Column {
	
	private String name;
	private String type;
	private String elementType;
	
	private boolean primaryKey;
	
	private List<Column> subColumns;

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

	public void setElementType(String elementType) {
		hasText(elementType);
		this.elementType = elementType;
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
}