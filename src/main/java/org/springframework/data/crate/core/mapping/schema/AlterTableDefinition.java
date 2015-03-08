/*
 * Copyright 2002-2015 the original author or authors.
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
import static org.springframework.util.Assert.notNull;

import java.util.List;

/**
 * {@link AlterTableDefinition} holds definition for columns and table parameters.
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AlterTableDefinition {
	
	private String name;
	
	private List<Column> alteredColumns;
	private List<AlterTableParameterDefinition> alteredParameters;
	
	public AlterTableDefinition(String tableName, List<Column> alteredColumns, List<AlterTableParameterDefinition> alteredParameters) {
		
		hasText(tableName);
		
		this.name = tableName;
		this.alteredColumns = alteredColumns;
		this.alteredParameters = alteredParameters;
	}
	
	public String getName() {
		return name;
	}

	public List<Column> getColumns() {
		return alteredColumns;
	}

	public List<AlterTableParameterDefinition> getAlteredParameters() {
		return alteredParameters;
	}

	public boolean hasAlteredColumns() {
		return (alteredColumns != null && !alteredColumns.isEmpty());
	}
	
	public boolean hasAlteredParameters() {
		return (alteredParameters != null && !alteredParameters.isEmpty());
	}
	
	public static class AlterTableParameterDefinition {
		
		private String parameterName;
		private Object parameterValue;
		
		public AlterTableParameterDefinition(String parameterName, Object parameterValue) {
			
			hasText(parameterName);
			notNull(parameterValue);
			
			this.parameterName = parameterName;
			this.parameterValue = parameterValue;
		}

		public String getParameterName() {
			return parameterName;
		}
		
		public Object getParameterValue() {
			return parameterValue;
		}
	}
}