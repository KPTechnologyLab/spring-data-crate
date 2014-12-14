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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.RESERVED_ID_FIELD_NAME;
import static org.springframework.data.mapping.model.BeanWrapper.create;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;
import io.crate.action.sql.SQLActionException;
import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;
import io.crate.client.CrateClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.CrateDocumentConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.crate.core.sql.AbstractStatement;
import org.springframework.data.crate.core.sql.CrateSQLStatement;
import org.springframework.data.crate.core.sql.Insert;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.StringUtils;

/**
 * @author Hasnain Javed
 * @author Rizwan Idrees
 * @since 1.0.0
 */
public class CrateTemplate implements CrateOperations {

    private final Logger logger = getLogger(CrateTemplate.class);
    
	private final CrateClient client;
	private final PersistenceExceptionTranslator exceptionTranslator;
    private CrateConverter crateConverter;
    
    private static final Collection<String> ITERABLE_CLASSES;
    
    private static final String NO_ID_WARNING = "Persitent Entity '{}' has no id property defined. Saving the same instance will result in a duplicate row";
    
    static {
        ITERABLE_CLASSES = unmodifiableCollection(asList(Collection.class.getName(),
        												 List.class.getName(),
        												 Iterator.class.getName(),
        												 Iterable.class.getName()));
    }
    
	public CrateTemplate(CrateClient client) {
        this(client, null);
	}

    public CrateTemplate(CrateClient client, CrateConverter crateConverter) {
        this.client  = client;
        this.crateConverter = crateConverter == null ? new MappingCrateConverter(new CrateMappingContext()) 
        											 : crateConverter;
        this.exceptionTranslator = new CrateExceptionTranslator();
    }

    @Override
    public CrateConverter getConverter() {
        return this.crateConverter;
    }

    @Override
	public SQLResponse execute(CrateSQLAction action) throws DataAccessException {
    	return this.execute(action, new CrateSQLResponseHandler<SQLResponse>() {
			@Override
			public SQLResponse handle(SQLResponse response) {
				return response;
			}
    	});
    }
    
    @Override
	public <T> T execute(CrateSQLAction action, CrateSQLResponseHandler<T> handler) throws DataAccessException {
    	
    	notNull(action, "An implementation of CrateSQLAction is required");
    	notNull(handler, "An implementation of CrateSQLResponseHandler<T> is required");
    	
    	try {
    		SQLRequest request = action.getSQLRequest();
    		logger.debug("executing query '{}' with args '{}'", request.stmt(), Arrays.toString(request.args()));
    		return (T)handler.handle(client.sql(request).actionGet());
    	}catch(SQLActionException e) {
    		throw tryConvertingRuntimeException(e);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
    }
    
    @Override
	public void save(Object objectToSave) {
    	notNull(objectToSave);
    	save(objectToSave, getTableName(objectToSave.getClass()));
	}

	@Override
	public void save(Object objectToSave, String tableName) {
		
		notNull(objectToSave);
		hasText(tableName);
		
		ensureNotCollectionType(objectToSave);
		
		boolean hasId = isIdPropertyDefined(objectToSave.getClass());
		
		if(!hasId) {
			logger.warn(NO_ID_WARNING, objectToSave.getClass().getName());
		}else {
			validateIdValue(objectToSave);
		}
		
		CrateDocument document = new CrateDocument();
		
		crateConverter.write(objectToSave, document);
		
		this.execute(new InsertAction(tableName, document));
	}
	
	@Override
	public <T> T findById(Object id, Class<T> entityClass) {
    	notNull(id);
		return findById(id, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> T findById(Object id, Class<T> entityClass, String tableName) {
		
		notNull(id);
		
		CratePersistentEntity<?> persistentEntity = getPersistentEntityFor(entityClass);
		
		String idColumn = null; 
				
		if(persistentEntity.hasIdProperty()) {
			idColumn = persistentEntity.getIdProperty().getFieldName();
		}else {
			logger.warn("Persitent Entity '{}' has no id property defined", entityClass.getName());
			idColumn = RESERVED_ID_FIELD_NAME;
			logger.info("Using fallback crate system column '{}' as primary key column name", idColumn);
		}
		
		return execute(new SelectByIdAction(tableName, idColumn, persistentEntity.getPropertyNames(), id),
					   new DefaultSQLResponseHandler<T>(entityClass));
	}
	
	@Override
	public <T> boolean removeById(Object object, Class<T> entityClass) {
		
		notNull(entityClass);
		return removeById(object, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> boolean removeById(Object object, Class<T> entityClass, String tableName) {
		
		notNull(entityClass);
		hasText(tableName);
		
		if(object == null) {
			return false;
		}
		
		CratePersistentProperty idProperty = getIdPropertyFor(entityClass);
		
		if (idProperty == null) {
			throw new MappingException("No id property found for object of type " + entityClass);
		}
		
		SQLResponse response = execute(new DeleteByIdAction(tableName, idProperty.getFieldName(), object));
		
		boolean removed = response.rowCount() == 1L;
		
		if(removed) {
			logger.debug("Removed row from crate with id '{}'", object);
		}else {
			logger.debug("No row removed from crate with id '{}'", object);
		}
		
		return removed;
	}
	
	private String getTableName(Class<?> clazz) {
		notNull(clazz, "Class parameter provided can not be null");
		return getPersistentEntityFor(clazz).getTableName();
	}
	
    private CratePersistentEntity<?> getPersistentEntityFor(Class<?> clazz) {
    	notNull(clazz, "Class parameter provided can not be null");
        return crateConverter.getMappingContext().getPersistentEntity(clazz);
    }
    
    /**
	 * Tries to convert the given {@link RuntimeException} into a {@link DataAccessException}. The original
	 * exception is returned if the conversion fails.
	 * 
	 * @param ex
	 * @return
	 */
	private RuntimeException tryConvertingRuntimeException(RuntimeException ex) {
		RuntimeException resolved = exceptionTranslator.translateExceptionIfPossible(ex);
		return resolved == null ? ex : resolved;
	}
	
	/**
	 * * Make sure the given object is not a iterable.
	   *
	   * @param objectToSave the object to verify.
	 */
	private void ensureNotCollectionType(Object objectToSave) {
		
		Class<?> clazz = objectToSave.getClass();
		
		if(clazz.isArray() || ITERABLE_CLASSES.contains(clazz.getName()) ||
		   Collection.class.isAssignableFrom(clazz) || Iterable.class.isAssignableFrom(clazz) || 
		   Iterator.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Cannot use a collection type for persisting entities.");
		}
	}
	
	private boolean isIdPropertyDefined(Class<?> clazz) {
		return getPersistentEntityFor(clazz).hasIdProperty();
	}
	
	private CratePersistentProperty getIdPropertyFor(Class<?> type) {
		return getPersistentEntityFor(type).getIdProperty();
	}
	
	private void validateIdValue(Object objectToSave) {
		
		CratePersistentProperty idProperty = getIdPropertyFor(objectToSave.getClass());
		
		if(idProperty != null) {
			
			BeanWrapper<Object> wrapper = create(objectToSave, crateConverter.getConversionService());
			
			Object idValue = wrapper.getProperty(idProperty);
			
			if(idValue == null) {
				throw new MappingException("Primary Key can not be null");
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class SelectByIdAction implements CrateSQLAction {
		
		private CrateSQLStatement select;
		private Object idValue;
		
		public SelectByIdAction(String tableName, String idColumn, Set<String> columns, Object idValue) {
			notNull(idValue);
			this.select = new Select(idColumn, tableName, columns);
			this.idValue = crateConverter.convertToCrateType(idValue, null);
		}
		
		@Override
		public SQLRequest getSQLRequest() {
			SQLRequest request = new SQLRequest(getSQLStatement(), new Object[]{idValue});
			request.includeTypesOnResponse(true);
			return request;
		}

		@Override
		public String getSQLStatement() {
			return select.createStatement();
		}
		
		// TODO: create a generic select statement in sql package
		private class Select extends AbstractStatement {
			
			private String idColumn;
			private String tableName;
			
			private Set<String> columns;
			
			public Select(String idColumn, String tableName, Set<String> columns) {
				
				hasText(tableName);
				hasText(idColumn);
				
				this.idColumn = idColumn;
				this.tableName = tableName;
				this.columns = columns != null ? columns : Collections.<String>emptySet();
			}
			
			@Override
			public String createStatement() {
				
				if(StringUtils.hasText(statement)) {
					return statement;
				}
				
				StringBuilder cols = new StringBuilder();
				
				Iterator<String> iterator = columns.iterator();
				
				while(iterator.hasNext()) {
					cols.append(doubleQuote(iterator.next()));
					if(iterator.hasNext()) {
						cols.append(", ");
					}
				}
				
				String colNames = StringUtils.hasText(cols.toString()) ? cols.toString() : "*";
				
				statement = format("SELECT %s FROM %s WHERE %s = ?", colNames, tableName, doubleQuote(idColumn));
				
				return statement;
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class InsertAction implements CrateSQLAction {
		
		private CrateSQLStatement insert;
		private CrateDocument document;
		
		public InsertAction(String tableName, CrateDocument document) {
			notEmpty(document);
			this.document = document;
			this.insert = new Insert(tableName, document.keySet());
		}
		
		@Override
		public SQLRequest getSQLRequest() {
			SQLRequest request = new SQLRequest(getSQLStatement(), document.values().toArray());
			request.includeTypesOnResponse(true);
			return request;
		}
		
		@Override
		public String getSQLStatement() {
			return insert.createStatement();
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class DeleteByIdAction implements CrateSQLAction {
		
		private CrateSQLStatement delete;
		private Object idValue;

		public DeleteByIdAction(String table, String idColumn, Object idValue) {
			this.delete = new Delete(table, idColumn);
			this.idValue = crateConverter.convertToCrateType(idValue, null);
		}

		@Override
		public SQLRequest getSQLRequest() {
			SQLRequest request = new SQLRequest(getSQLStatement(), new Object[]{idValue});
			request.includeTypesOnResponse(true);
			return request;
		}

		@Override
		public String getSQLStatement() {
			return delete.createStatement();
		}
		
		// TODO: create a generic delete statement in sql package
		private class Delete extends AbstractStatement {

			private String table;
			private String idColumn;
			
			public Delete(String table, String idColumn) {
				
				hasText(table);
				hasText(idColumn);
				
				this.table = table;
				this.idColumn = idColumn;
			}
			
			@Override
			public String createStatement() {
				
				if(StringUtils.hasText(statement)) {
					return statement;
				}
				
				statement = format("DELETE FROM %s WHERE %s = ?", table, doubleQuote(idColumn));
				
				return statement;
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	private class DefaultSQLResponseHandler<T> implements CrateSQLResponseHandler<T> {

		private final Class<T> type;
		
		public DefaultSQLResponseHandler(Class<T> type) {
			notNull(type);
			this.type = type;
		}
		
		@Override
		public T handle(SQLResponse response) {
			
			CrateDocument source = new CrateDocumentConverter(response).toDocument();
			
			return (T)crateConverter.read(type, source);
		}
	}
}