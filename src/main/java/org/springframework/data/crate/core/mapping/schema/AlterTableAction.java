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

import static java.lang.String.format;
import static org.springframework.data.crate.core.mapping.CrateDataType.OBJECT;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.split;
import io.crate.action.sql.SQLRequest;

import java.util.Iterator;

import org.springframework.data.crate.core.CrateSQLAction;
import org.springframework.util.StringUtils;

/**
 * {@link TableDefinition} holds crate specific ddl for altering table.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
class AlterTableAction implements CrateSQLAction {
	
	private static final String SQL_PATH_TEMPLATE = "['%s']";
	
	private String statement;
	private String tableName;
	private Column column;	
	
	public AlterTableAction(String tableName, Column column) {
		hasText(tableName);
		notNull(column);
		
		this.tableName = tableName;
		this.column = column;
	}

	@Override
	public SQLRequest getSQLRequest() {
		return new SQLRequest(getSQLStatement());
	}

	@Override
	public String getSQLStatement() {
		statement = StringUtils.hasText(statement) ? statement : createStatement();
		return statement;
	}
	
	private String createStatement() {
		
		StringBuilder builder = new StringBuilder(ALTER_TABLE).append(SPACE).
															   append(tableName).
															   append(SPACE).
															   append(ADD_COLUMN).
															   append(SPACE).
															   append("\""). // double quotes to preserve case in crate db
															   append(toSqlPath(column)).
															   append("\""). // double quotes to preserve case in crate db
															   append(SPACE).
															   append(createColumnDefinition(column));
		return builder.toString();
	}
	
	private String toSqlPath(Column column) {
		
		String[] tokens = org.apache.commons.lang.StringUtils.split(column.getName(), ".");
		
		if(tokens == null) {
			return column.getName();
		}
		
		StringBuilder sqlPath = new StringBuilder(tokens[0]);
		
		for (int i = 1; i < tokens.length; i++) {
			sqlPath.append(format(SQL_PATH_TEMPLATE, tokens[i]));
		}
		
		return sqlPath.toString();
	}
	
	private String createColumnDefinition(Column column) {
		
		StringBuilder builder = new StringBuilder();
		
		if(column.isArrayColumn()) {
			builder.append(column.getCrateType());
			builder.append(OPEN_BRACE);
			if(column.isPrimitiveElementType(column.getElementCrateType())) {
				builder.append(column.getElementCrateType());
			}else {
				createObjectColumnStatement(column, builder);
			}
			
			builder.append(CLOSE_BRACE);
		}else if(column.isObjectColumn()) {
			createObjectColumnStatement(column, builder);
		}else {
			builder.append(column.getCrateType());
		}
		
		if(column.isPrimaryKey()) {
			builder.append(SPACE)
				   .append(PRIMARY_KEY);
		}
		
		return builder.toString();
	}
	
	/*private void toSqlPath(Column column, StringBuilder pathBuilder, String path) {
		if(column.isObjectColumn()) {
			String sqlPath = StringUtils.hasText(path) ? format(SQL_PATH_TEMPLATE, column.getName()) : column.getName();
			pathBuilder.append(sqlPath);
			for(Column subColumn : column.getSubColumns()) {
				toSqlPath(subColumn, pathBuilder, sqlPath);
			}
		}
	}*/
	
	private void createObjectColumnStatement(Column column, StringBuilder builder) {
		
		builder.append(OBJECT);
		
		if(!column.getSubColumns().isEmpty()) {
			
			builder.append(SPACE)
				   .append(AS)
				   .append(SPACE)
			   	   .append(OPEN_BRACE);
			
			Iterator<Column> subColumns = column.getSubColumns().iterator();
			
			while(subColumns.hasNext()) {
				createColumnStatement(subColumns.next(), builder);
				if(subColumns.hasNext()) {
					builder.append(COMMA)
						   .append(SPACE);
				}
			}
			
			builder.append(CLOSE_BRACE);
		}
	}
	
	private void createColumnStatement(Column column, StringBuilder builder) {
		
		// double quotes to preserve case in crate db
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
				createObjectColumnStatement(column, builder);
			}
			
			builder.append(CLOSE_BRACE);
		}else if(column.isObjectColumn()) {
			createObjectColumnStatement(column, builder);
		}else {
			builder.append(column.getCrateType());
		}
		
		if(column.isPrimaryKey()) {
			builder.append(SPACE)
				   .append(PRIMARY_KEY);
		}
	}

	/*private String createSqlPath(String name, String sqlPath) {
//		String path = null;
		
		return StringUtils.hasText(sqlPath) ? sqlPath.concat(format(SQL_PATH_TEMPLATE, name)) : name; 
		
		if(StringUtils.hasText(sqlPath)) {
			sqlPath.concat(format(SQL_PATH_TEMPLATE, name));
		}else {
			path = "\"".concat(name).concat("\"");
		}
		return path;
	}*/
}