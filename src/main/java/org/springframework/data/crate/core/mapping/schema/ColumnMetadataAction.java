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
import static java.lang.String.valueOf;
import static org.springframework.util.Assert.hasText;
import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.crate.NoSuchTableException;
import org.springframework.data.crate.core.CrateAction;
import org.springframework.data.crate.core.CrateActionResponseHandler;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
class ColumnMetadataAction implements CrateAction, CrateActionResponseHandler<List<ColumnMetadata>> {

    private static final String SELECT_TEMPLATE = "select table_name, column_name, data_type from information_schema.columns where table_name = '%s'";
    
    private final String tableName;
    private final String statement;
    
	public ColumnMetadataAction(String tableName) {
		super();
		hasText(tableName);			
		this.tableName = tableName.toLowerCase();
		this.statement = format(SELECT_TEMPLATE, this.tableName);
	}
	
	@Override
	public SQLRequest getSQLRequest() {
		return new SQLRequest(getSQLStatement());
	}
	
	@Override
	public String getSQLStatement() {
		return statement;
	}
	
	@Override
	public List<ColumnMetadata> handle(SQLResponse response) {
		
		if(response.rows().length > 0) {
			
			List<ColumnMetadata> columns = new LinkedList<ColumnMetadata>();
			
			Object[][] rows = response.rows();
			
			for (int i = 0; i < rows.length; i++) {
				
				String sqlPath = valueOf(rows[i][1]);
				String dataType = valueOf(rows[i][2]);
				
				columns.add(new ColumnMetadata(sqlPath, dataType));
			}
			
			return columns;
		}
		
		throw new NoSuchTableException(format("Table '%s' has no metadata in 'information_schema.columns'. Table does not exist", tableName), null);
	}
}