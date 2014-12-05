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
package org.springframework.data.crate.core.sql;

import static org.springframework.data.crate.core.convert.CrateTypeMapper.DEFAULT_TYPE_KEY;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.data.crate.core.mapping.CrateDataType.STRING;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasText;

import java.util.Iterator;

import org.springframework.data.crate.core.mapping.schema.Column;
import org.springframework.data.crate.core.mapping.schema.TableDefinition;

/**
 * {@link CreateTable} creates crate specific ddl for creating table.
 * 
 * @author Hasnain Javed
 * @since 1.0.0 
 */
public class CreateTable extends AbstractStatement {
	
	private TableDefinition tableDefinition;

	public CreateTable(TableDefinition tableDefinition) {
		notNull(tableDefinition);
		this.tableDefinition = tableDefinition;
	}
	
	@Override
	public String createStatement() {
		
		if(!hasText(statement)) {
			
			StringBuilder builder = new StringBuilder(CREATE_TABLE).append(SPACE)
																   .append(tableDefinition.getName())
																   .append(SPACE)
																   .append(OPEN_BRACE)
																   .append(doubleQuote(DEFAULT_TYPE_KEY))
																   .append(SPACE)
																   .append(STRING)
																   .append(COMMA)
																   .append(SPACE);
			
			Iterator<Column> iterator = tableDefinition.getColumns().iterator();
			
			while(iterator.hasNext()) {
				
				createStatement(iterator.next(), builder);
				if(iterator.hasNext()) {
					builder.append(COMMA)
						   .append(SPACE);
				}
			}
			
			builder.append(CLOSE_BRACE);
			
			statement = builder.toString();
		}
		
		return statement;
	}
	
	private void createStatement(Column column, StringBuilder builder) {
		
		// double quotes to preserve case in crate db
		builder.append(doubleQuote(column.getName()));
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