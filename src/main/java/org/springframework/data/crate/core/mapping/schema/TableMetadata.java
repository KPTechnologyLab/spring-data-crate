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

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;

import java.util.List;

/**
 * {@link TableMetadata} holds metadata information for table and it's columns.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class TableMetadata {
	
	private String name;
	private List<ColumnMetadata> columns;
	
	public TableMetadata(String name, List<ColumnMetadata> columns) {
		super();
		hasText(name);
		notEmpty(columns);
		this.name = name;
		this.columns = columns;
	}
	
	public String getName() {
		return name;
	}

	public List<ColumnMetadata> getColumns() {
		return columns;
	}
 }