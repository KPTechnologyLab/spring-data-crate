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

package org.springframework.data.crate.core;

import io.crate.action.sql.SQLRequest;

/**
 * @author Hasnain Javed
 *
 * @since 1.0.0
 */
public interface CrateSQLAction {
	
	String SPACE = " ";
	String COMMA = ",";
	String OPEN_BRACE = "(";
	String CLOSE_BRACE = ")";
	String AS = "as";
	String PRIMARY_KEY = "primary key";
	String CREATE_TABLE = "create table";
	String DROP_TABLE = "drop table";
	String ALTER_TABLE = "alter table";
	
	public SQLRequest getSQLRequest();
	public String getSQLStatement();
}