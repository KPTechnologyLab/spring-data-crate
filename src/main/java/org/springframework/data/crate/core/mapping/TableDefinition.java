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

package org.springframework.data.crate.core.mapping;

import static java.util.Collections.emptyList;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.Iterator;
import java.util.List;

/**
 * {@link TableDefinition} holds definition for table and columns. Generates crate specific ddl for table.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
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