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

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.UPDATE;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.values;
import static org.springframework.util.Assert.notNull;
import io.crate.action.sql.SQLRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.crate.NoSuchTableException;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.CrateSQLAction;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.crate.core.sql.AlterTable;
import org.springframework.data.crate.core.sql.CrateSQLStatement;
import org.springframework.data.crate.core.sql.CreateTable;
import org.springframework.data.crate.core.sql.DropTable;
import org.springframework.data.mapping.context.MappingContext;

/**
 * Component that inspects {@link CratePersistentEntity} instances contained in the given {@link CrateMappingContext}
 * for creating/altering tables in Crate DB.
 *
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CratePersistentEntitySchemaManager implements InitializingBean, DisposableBean {

	private final Logger logger = getLogger(getClass());
	private final Map<Class<?>, Boolean> inspectedEntities;
	private final SchemaExportOption DEFAULT_SCHEMA_EXPORT_OPTION = UPDATE;
	
	private MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;	
	private CrateOperations crateOperations;
	private SchemaExportOption exportOption;	
	private CratePersistentEntityTableManager tableManager;
	
	private boolean ignoreFailures = false;
	
	/**
	 * Creates a new {@link CratePersistentEntitySchemaManager} for the given {@link CrateOperations}
	 * 
	 * @param crateOperations must not be {@literal null}.
	 */
	public CratePersistentEntitySchemaManager(CrateOperations crateOperations) {
		super();
		notNull(crateOperations);
		
		this.crateOperations = crateOperations;
		this.exportOption = DEFAULT_SCHEMA_EXPORT_OPTION; 
		this.mappingContext = crateOperations.getConverter().getMappingContext();
		this.tableManager = new CratePersistentEntityTableManager(mappingContext);
		this.inspectedEntities = new ConcurrentHashMap<Class<?>, Boolean>();
	}
	
	/**
	* Flag to indicate that all failures in SQL statement(s) should be logged but not cause the exception to propagate.
	* Defaults to {@code true}.
	* @param ignoreFailures {@code false} if exporting tables should not continue on failure
	*/
	public void setIgnoreFailures(boolean ignoreFailures) {
		this.ignoreFailures = ignoreFailures;
	}

	/**
	* Configures the {@link SchemaExportOption} to be used for exporting tables. Setting {@literal null} will reset the
	* default of {@value #DEFAULT_SCHEMA_EXPORT_OPTION}.
	* @param exportOption
	*/
	public void setExportOption(SchemaExportOption exportOption) {		
		this.exportOption = exportOption == null ? DEFAULT_SCHEMA_EXPORT_OPTION : exportOption;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		execute();
	}
	
	@Override
	public void destroy() throws Exception {
		
		if(exportOption == CREATE_DROP) {
			for(Class<?> clazz : inspectedEntities.keySet()) {
				dropTable(mappingContext.getPersistentEntity(clazz));
			}
		}
	}
	
	private void execute() {
		
		try {
			switch (exportOption) {
			case CREATE:
			case CREATE_DROP:
				dropAndCreateTables();
				break;
			case UPDATE:
				updateTables();
				break;
			default:
				throw new IllegalArgumentException(format("unknown SchemaExportOption %s. valid values are %s", exportOption,
																										  		Arrays.toString(values())));
			}
		}catch(DataAccessException e) {
			if(!ignoreFailures) {
				throw e;
			}else {
				logger.warn(e.getMessage());
			}
		}
	}
	
	private void dropAndCreateTables() {
		
		for(CratePersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
			dropTable(entity);
			createTable(entity);
			addInspectedEntity(entity);
		}
	}
	
	private void updateTables() {
		
		for(CratePersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
			
			try {
				alterTable(entity);
			}catch(NoSuchTableException e) {
				logger.info(e.getMessage());
				createTable(entity);
			}
			
			addInspectedEntity(entity);
		}
	}
	
	private void dropTable(CratePersistentEntity<?> entity) {
		try {
			crateOperations.execute(new DropTableAction(entity.getTableName()));
			logger.info("dropped table '{}' for '{}'", entity.getTableName(), entity.getType());
		}catch(InvalidDataAccessResourceUsageException e) {
			logger.warn(e.getMessage());
		}
	}
	
	private void createTable(CratePersistentEntity<?> entity) {
		TableDefinition tableDefinition = tableManager.createDefinition(entity);
		crateOperations.execute(new CreateTableAction(tableDefinition));
		logger.info("created table '{}' for '{}'", entity.getTableName(), entity.getType());
	}
	
	private void alterTable(CratePersistentEntity<?> entity) {
		
		TableMetadata tableMetadata = getTableMetadata(entity);
		
		TableDefinition tableDefinition = tableManager.updateDefinition(entity, tableMetadata);
		
		if(tableDefinition != null) {
			for(Column column : tableDefinition.getColumns()) {
				crateOperations.execute(new AlterTableAction(tableDefinition.getName(), column));
				logger.info("altered table '{}' for '{}'", entity.getTableName(), entity.getType());
			}
		}else {
			logger.info("entity '{}' and crate db table '{}' are insynch", entity.getName(), 
																		   tableMetadata.getName());
		}
	}

	private void addInspectedEntity(CratePersistentEntity<?> entity) {
		Class<?> type = entity.getType();
		if (!inspectedEntities.containsKey(type)) {
			inspectedEntities.put(type, TRUE);
		}
	}
	
	private TableMetadata getTableMetadata(CratePersistentEntity<?> entity) {
		ColumnMetadataAction action = new ColumnMetadataAction(entity.getTableName());
		List<ColumnMetadata> columns = crateOperations.execute(action, action);
		return new TableMetadata(entity.getTableName(), columns);
	}
	
	 /**
	 * {@link CreateTableAction} implementation of {@link CrateSQLAction} to execute create table command.
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class CreateTableAction implements CrateSQLAction {
		
		private CrateSQLStatement createTable;
		
		public CreateTableAction(TableDefinition tableDefinition) {
			super();
			this.createTable = new CreateTable(tableDefinition);
		}

		@Override
		public SQLRequest getSQLRequest() {
			return new SQLRequest(getSQLStatement());
		}
		
		@Override
		public String getSQLStatement() {
			return createTable.createStatement();
		}
	}
	
	/**
	 * {@link AlterTableAction} implementation of {@link CrateSQLAction} to execute alter table command.
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class AlterTableAction implements CrateSQLAction {
		
		private CrateSQLStatement alterTable;
		
		public AlterTableAction(String tableName, Column column) {
			this.alterTable = new AlterTable(tableName, column);
		}

		@Override
		public SQLRequest getSQLRequest() {
			return new SQLRequest(getSQLStatement());
		}

		@Override
		public String getSQLStatement() {
			return alterTable.createStatement();
		}
	}
	
	/**
	 * {@link DropTableAction} implementation of {@link CrateSQLAction} to execute drop table command.
	 * @author Hasnain Javed 
	 * @since 1.0.0
	 */
	class DropTableAction implements CrateSQLAction {
		
		private CrateSQLStatement dropTable;
		
		public DropTableAction(String tableName) {
			super();
			this.dropTable = new DropTable(tableName);
		}

		@Override
		public SQLRequest getSQLRequest() {
			return new SQLRequest(getSQLStatement());
		}

		@Override
		public String getSQLStatement() {
			return dropTable.createStatement();
		}
	}
}