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

import static org.springframework.data.crate.core.sql.CrateSQLUtil.dotToSqlPath;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import org.springframework.data.crate.core.mapping.schema.Column;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0 
 */
public abstract class AbstractStatement implements CrateSQLStatement {
	
	protected String statement;

	public String getStatement() {
		return statement;
	}
	
	protected String doubleQuote(String toQuote) {
		hasText(toQuote);
		return CrateSQLUtil.doubleQuote(toQuote);
	}
	
	protected String singleQuote(String toQuote) {
		hasText(toQuote);
		return CrateSQLUtil.singleQuote(toQuote);
	}
	
	protected String toSqlPath(Column column) {
		notNull(column);
		return dotToSqlPath(column.getName());
	}
}