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

import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import io.crate.action.sql.SQLRequest;

import java.util.Iterator;

import org.springframework.data.crate.core.CrateSQLAction;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.*;

/**
 * {@link CreateTableAction} holds crate specific ddl for creating table.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
class CreateTableAction implements CrateSQLAction {
	
	private String statement;
	private TableDefinition tableDefinition;

	public CreateTableAction(TableDefinition tableDefinition) {
		super();
		notNull(tableDefinition);
		this.tableDefinition = tableDefinition;
	}

	@Override
	public SQLRequest getSQLRequest() {
		return new SQLRequest(getSQLStatement());
	}
	
	@Override
	public String getSQLStatement() {
		statement = hasText(statement) ? statement : createStatement();
		return statement;
	}
	
	private String createStatement() {
		
		StringBuilder builder = new StringBuilder(CREATE_TABLE).append(SPACE)
															   .append(tableDefinition.getName())
															   .append(SPACE)
															   .append(OPEN_BRACE);
		
		Iterator<Column> iterator = tableDefinition.getColumns().iterator();
		
		while(iterator.hasNext()) {
			createStatement(iterator.next(), builder);
			if(iterator.hasNext()) {
				builder.append(COMMA)
					   .append(SPACE);
			}
		}
		
		builder.append(CLOSE_BRACE);
		
		return builder.toString();
	}
	
	private void createStatement(Column column, StringBuilder builder) {
		
		// double quotes to preserve case
		builder.append("\"");
		builder.append(column.getName());
		builder.append("\"");
		builder.append(SPACE);
		
		if(column.isArrayColumn()) {
			builder.append(column.getCrateType());
			builder.append(OPEN_BRACE);
			if(column.isPrimitiveElementType(column.getElementCrateType())) {
				builder.append(column.getElementCrateType());
			}else {
				createObjectColumn(column, builder);
			}
			
			builder.append(CLOSE_BRACE);
		}else if(column.isObjectColumn()) {
			createObjectColumn(column, builder);
		}else {
			builder.append(column.getCrateType());
		}
		
		if(column.isPrimaryKey()) {
			builder.append(SPACE)
				   .append(PRIMARY_KEY);
		}
	}
	
	private void createObjectColumn(Column column, StringBuilder builder) {
		
		builder.append(OBJECT);
		
		if(!column.getSubColumns().isEmpty()) {
			
			builder.append(SPACE)
				   .append(AS)
				   .append(SPACE)
			   	   .append(OPEN_BRACE);
			
			Iterator<Column> subColumns = column.getSubColumns().iterator();
			
			while(subColumns.hasNext()) {
				createStatement(subColumns.next(), builder);
				if(subColumns.hasNext()) {
					builder.append(COMMA)
						   .append(SPACE);
				}
			}
			
			builder.append(CLOSE_BRACE);
		}
	}
}