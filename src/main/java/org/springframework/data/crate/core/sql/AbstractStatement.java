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

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.split;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import org.springframework.data.crate.core.mapping.schema.Column;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0 
 */
public abstract class AbstractStatement implements CrateSQLStatement {
	
	private static final String DOUBLE_QUOTE_TEMPLATE = "\"%s\"";
	private static final String SQL_PATH_TEMPLATE = "['%s']";
	
	protected String statement;

	public String getStatement() {
		return statement;
	}
	
	protected String doubleQuote(String toQuote) {
		hasText(toQuote);
		return format(DOUBLE_QUOTE_TEMPLATE, toQuote);
	}
	
	protected String toSqlPath(Column column) {
		
		String[] tokens = split(column.getName(), ".");
		
		// double quotes to preserve case in crate db
		StringBuilder sqlPath = new StringBuilder(doubleQuote(tokens[0]));
		
		for (int i = 1; i < tokens.length; i++) {
			sqlPath.append(format(SQL_PATH_TEMPLATE, tokens[i]));
		}
		
		return sqlPath.toString();
	}
}