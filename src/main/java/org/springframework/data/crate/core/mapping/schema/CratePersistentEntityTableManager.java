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

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.sql.CrateSQLUtil.sqlToDotPath;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasText;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

/**
 * Component that generates table definition from {@link CratePersistentEntity} instances
 * for creating/altering tables in Crate DB.
 *
 * @author Hasnain Javed 
 * @since 1.0.0
 */
public class CratePersistentEntityTableManager {
	
	private final Logger logger = getLogger(getClass());
	
	private final EntityColumnMapper entityColumnMapper;

	/**
	 * Creates a new {@link CratePersistentEntityTableManager} for the given {@link CrateMappingContext}
	 * 
	 * @param mappingContext must not be {@literal null}.
	 */
	public CratePersistentEntityTableManager(MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		super();
		notNull(mappingContext);
		this.entityColumnMapper = new EntityColumnMapper(mappingContext);
	}
	
	/**
	 * Creates table definition containing table information the columns. 
	 * @param entity instance used to generate table definition
	 * @return table definition with column definitions  
	 */
	public TableDefinition createDefinition(CratePersistentEntity<?> entity) {
		notNull(entity);		
		List<Column> columns = entityColumnMapper.toColumns(entity);
		return new TableDefinition(entity.getTableName(), columns);
	}
	
	/**
	 * Creates table definition containing table information and columns that are not
	 * found in {@link TableMetadata}
	 * @param entity instance used to generate table definition
	 * @param tableMetadata metadata associated to the {@link CratePersistentEntity}
	 * @return table with column definitions. If there is no difference in
	 *  	   {@link CratePersistentEntity} instance and the {@link TableMetadata} information,
	 *  	   null is returned
	 */
	public TableDefinition updateDefinition(CratePersistentEntity<?> entity, TableMetadata tableMetadata) {
		
		List<Column> columns = entityColumnMapper.toColumns(entity);
		
		Map<String, Column> columnPaths = columnToDotPath(columns);
		Map<String, String> sqlPaths = convertToDotPath(tableMetadata.getColumns());
		
		List<Column> additionalColumns = new LinkedList<Column>();
		
		Iterator<Entry<String, Column>> iterator = columnPaths.entrySet().iterator();
		
		while(iterator.hasNext()) {
			
			Entry<String, Column> columnPath = iterator.next();			
			if(!sqlPaths.containsKey(columnPath.getKey())) {
				
				logger.debug("adding new column under path '{}'", columnPath.getKey());
				Column addedColumn = new Column(columnPath.getKey(), 
												columnPath.getValue().getRawType(), 
												columnPath.getValue().getElementRawType());
				addedColumn.setSubColumns(columnPath.getValue().getSubColumns());
				
				additionalColumns.add(addedColumn);
				
				if(columnPath.getValue().isObjectColumn() || columnPath.getValue().isObjectArrayColumn()) {
					removePropertyPaths(columnPath.getKey(), iterator);
				}
			}
		}
		
		return additionalColumns.isEmpty() ? null : new TableDefinition(entity.getTableName(), additionalColumns);
	}
	
	private void removePropertyPaths(String dotPath, Iterator<Entry<String, Column>> columns) {
		
		while(columns.hasNext()) {
			Entry<String, Column> columnPath = columns.next();
			if(columnPath.getKey().startsWith(dotPath)) {
				logger.info("removing column path '{}' where sql path is '{}'", columnPath.getKey(), dotPath);
				columns.remove();
			}
		}
	}
	
	private Map<String, Column> columnToDotPath(List<Column> columns) {
		
		LinkedHashMap<String, Column> map = new LinkedHashMap<String, Column>();
		
		for(Column column : columns) {
			logger.debug("pushing column under key '{}'", column.getName());
			map.put(column.getName(), column);
			columnToDotPath(column, map, column.getName());
		}
		
		return map;
	}
	
	private void columnToDotPath(Column column, Map<String, Column> map, String columnName) {
		
		Iterator<Column> subColumns = column.getSubColumns().iterator();
		
		while(subColumns.hasNext()) {
			Column subColumn = subColumns.next();
			String dotPath = createdotPathKey(columnName, subColumn.getName());
			logger.debug("pushing column under key '{}'", dotPath);
			map.put(dotPath, subColumn);
			columnToDotPath(subColumn, map, dotPath);
		}
	}
	
	private String createdotPathKey(String rootPropertyName, String propertyName) {
		return hasText(rootPropertyName) ? rootPropertyName.concat(".").concat(propertyName) : 
										   propertyName;
	}
	
	private Map<String, String> convertToDotPath(List<ColumnMetadata> columns) {
		
		Map<String, String> sqlPaths = new LinkedHashMap<String, String>(columns.size());
		
		for(ColumnMetadata metadata : columns) {
			
			String dotPath = sqlToDotPath(metadata.getSqlPath());
			String type = metadata.getCrateType();
			logger.debug("pushing sqlPath under key '{}'", dotPath);
			sqlPaths.put(dotPath, type);
		}
		
		return sqlPaths;
	}
}