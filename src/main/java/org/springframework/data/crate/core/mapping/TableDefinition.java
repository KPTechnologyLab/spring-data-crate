package org.springframework.data.crate.core.mapping;

import static java.util.Collections.emptyList;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.Iterator;
import java.util.List;

class TableDefinition {
	
	public static final String SPACE = " ";
	public static final String COMMA = ",";
	public static final String OPEN_BRACE = "(";
	public static final String CLOSE_BRACE = ")";
	public static final String AS = "as";
	public static final String OBJECT = "object";
	public static final String PRIMARY_KEY = "primary key";
	
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
		
		if(columns == null) {
			return emptyList();
		}
		
		return columns;
	}
	
	public void setColumns(List<Column> columns) {
		notNull(columns);
		this.columns = columns;
	}
	
	public String toSqlStatement() {
		
		StringBuilder builder = new StringBuilder("create table").append(SPACE)
																 .append(getName())
																 .append(SPACE)
																 .append(OPEN_BRACE);
		Iterator<Column> iterator = columns.iterator();
		
		while(iterator.hasNext()) {
			builder.append(iterator.next().toSqlStatement());
			if(iterator.hasNext()) {
				builder.append(COMMA)
					   .append(SPACE);
			}
		}
		
		builder.append(CLOSE_BRACE);
		
		return builder.toString();
	}
}