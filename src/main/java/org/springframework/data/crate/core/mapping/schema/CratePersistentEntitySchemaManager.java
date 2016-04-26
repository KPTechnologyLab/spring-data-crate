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
import org.springframework.data.crate.NoSuchTableException;
import org.springframework.data.crate.core.CrateAction;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.CrateSQLAction;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.crate.core.mapping.schema.AlterTableDefinition.AlterTableParameterDefinition;
import org.springframework.data.crate.core.sql.AlterTable;
import org.springframework.data.crate.core.sql.AlterTableParameter;
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
	
	private MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;	
	private CrateOperations crateOperations;
	private SchemaExportOption exportOption;	
	private CratePersistentEntityTableManager tableManager;
	
	private boolean ignoreFailures;
	private boolean enabled;
	
	/**
	 * Creates a new {@link CratePersistentEntitySchemaManager} for the given {@link CrateOperations}
	 * 
	 * @param crateOperations must not be {@literal null}.
	 */
	public CratePersistentEntitySchemaManager(CrateOperations crateOperations, SchemaExportOption exportOption) {
		super();
		notNull(crateOperations);
		notNull(exportOption);
		this.crateOperations = crateOperations;
		this.exportOption = exportOption; 
		this.mappingContext = crateOperations.getConverter().getMappingContext();
		this.tableManager = new CratePersistentEntityTableManager(mappingContext);
		this.inspectedEntities = new ConcurrentHashMap<>();
		this.ignoreFailures = false;
		this.enabled = true;
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
	* Flag to explicitly enable or disable exporting schema to crate db.
	* @param enabled {@code true} if the schema should be exported
	*/
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(enabled) {
			export();
		}else {
			logger.debug("skipping scehma export as the schema manager is disabled");
		}
	}
	
	@Override
	public void destroy() throws Exception {
		
		if(exportOption == CREATE_DROP) {
			for(Class<?> clazz : inspectedEntities.keySet()) {
				dropTable(mappingContext.getPersistentEntity(clazz));
			}
		}
	}
	
	private void export() {
		
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
		}catch(NoSuchTableException e) {
			logger.warn("table '{}' does not exist for '{}'", entity.getTableName(), entity.getType());
		}
	}
	
	private void createTable(CratePersistentEntity<?> entity) {
		TableDefinition def = tableManager.createDefinition(entity);
		crateOperations.execute(new CreateTableAction(def));
		logger.info("created table '{}' for '{}'", entity.getTableName(), entity.getType());
	}
	
	private void alterTable(CratePersistentEntity<?> entity) {
		
		TableMetadata tableMetadata = getTableMetadata(entity);
		
		AlterTableDefinition def = tableManager.alterDefinition(entity, tableMetadata);
		
		if(def.hasAlteredParameters()) {
			for(AlterTableParameterDefinition paramDef : def.getTableParameters()) {
				
				crateOperations.execute(new AlterTableAction(new AlterTableParameter(def.getName(), paramDef)));
				
				logger.info("altered table '{}' for '{}' with parameter '{}'", new Object[]{entity.getTableName(), entity.getType(), 
																							paramDef.getParameterName()});
			}
		}
		
		if(def.hasAlteredColumns()) {
			for(Column column : def.getColumns()) {
				
				crateOperations.execute(new AlterTableAction(new AlterTable(def.getName(), column)));
				logger.info("altered table '{}' for '{}' with column '{}'", new Object[]{entity.getTableName(), entity.getType(), 
																						 column.getName()});
			}
		}
		
		if(!def.hasAlteredColumns() && !def.hasAlteredParameters()) {
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
		
		TableMetadataAction action = new TableMetadataAction(entity, getColumnMetadata(entity));
		return crateOperations.execute(action, action);
	}
	
	private List<ColumnMetadata> getColumnMetadata(CratePersistentEntity<?> entity) {
		ColumnMetadataAction action = new ColumnMetadataAction(entity.getTableName());
		return crateOperations.execute(action, action);
	}
	
	 /**
	 * {@link CreateTableAction} implementation of {@link CrateSQLAction} to execute create table command.
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class CreateTableAction implements CrateAction {
		
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
	private class AlterTableAction implements CrateAction {
		
		private CrateSQLStatement alterStatement;
		
		public AlterTableAction(CrateSQLStatement alterStatement) {
			this.alterStatement = alterStatement;
		}

		@Override
		public SQLRequest getSQLRequest() {
			return new SQLRequest(getSQLStatement());
		}

		@Override
		public String getSQLStatement() {
			return alterStatement.createStatement();
		}
	}
	
	/**
	 * {@link DropTableAction} implementation of {@link CrateSQLAction} to execute drop table command.
	 * @author Hasnain Javed 
	 * @since 1.0.0
	 */
	class DropTableAction implements CrateAction {
		
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