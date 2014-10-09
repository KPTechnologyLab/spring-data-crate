package org.springframework.data.crate.core.mapping;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.List;

class TableDefinition {
	
	private String name;
	
	private List<Column> columns;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		hasText(name);
		this.name = name;
	}
	
	public List<Column> getColumns() {
		return columns;
	}
	
	public void setColumns(List<Column> columns) {
		notNull(columns);
		this.columns = columns;
	}
}