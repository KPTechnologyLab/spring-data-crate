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
import static org.apache.commons.lang.ArrayUtils.add;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.convert.CrateTypeMapper.DEFAULT_TYPE_KEY;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.INITIAL_VERSION_VALUE;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.RESERVED_ID_FIELD_NAME;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.RESERVED_VESRION_FIELD_NAME;
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
    
    private static final String PRIMARY_KEY = "Primary Key must not be null";
    private static final String SQL_STATEMENT = "executing statement '{}' with args '{}'";
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
    		logger.debug(SQL_STATEMENT, request.stmt(), Arrays.toString(request.args()));
    		return (T)handler.handle(client.sql(request).actionGet());
    	}catch(SQLActionException e) {
    		throw tryConvertingRuntimeException(e);
		}
    }
    
    @Override
	public void insert(Object entity) {
    	notNull(entity);
    	insert(entity, getTableName(entity.getClass()));
	}

	@Override
	public void insert(Object entity, String tableName) {
		
		notNull(entity);
		hasText(tableName);
		
		ensureNotCollectionType(entity);
		
		boolean hasId = isIdPropertyDefined(entity.getClass());

		if(!hasId) {
			logger.warn(NO_ID_WARNING, entity.getClass().getName());
		}else {
			validateIdValue(entity);
		}
		
		CrateDocument document = new CrateDocument();
		
		crateConverter.write(entity, document);
		
		CratePersistentProperty versionProperty = getPersistentEntityFor(entity.getClass()).getVersionProperty();
		
		this.execute(new InsertAction(tableName, document));
		
		if(isVersionPropertyDefined(entity.getClass())) {
			BeanWrapper<Object> wrapper = create(entity, crateConverter.getConversionService());
			wrapper.setProperty(versionProperty, INITIAL_VERSION_VALUE);
		}
	}
	
	@Override
	public void update(Object entity) {
		
		notNull(entity);
		update(entity, getTableName(entity.getClass()));
	}
	
	@Override
	public void update(Object entity, String tableName) {
		
		notNull(entity);
		hasText(tableName);
		
		ensureNotCollectionType(entity);
		
		CratePersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
		
		String idColumn = null;
		Object idValue = null;
		
		if(!persistentEntity.hasIdProperty()) {
			throw new MappingException("Persistent Entity '{}' must define an id column");
		}else {
			idColumn = persistentEntity.getIdProperty().getFieldName();
		}
		
		validateIdValue(entity);

		idValue = getIdPropertyValue(entity);
		
		CrateDocument document = new CrateDocument();
		
		crateConverter.write(entity, document);
		
		document.remove(idColumn);
		document.remove(DEFAULT_TYPE_KEY);
		
		if(persistentEntity.hasVersionProperty()) {
			document.remove(persistentEntity.getVersionProperty().getFieldName());
		}
		
		SQLResponse response = this.execute(new WholesaleUpdateByIdAction(tableName, idColumn, document, idValue));
		
		if(response.rowCount() < 0) {
			logger.info("No row updated with id '{}'", idValue);
		}else {
			logger.info("Updated row with id '{}'", idValue);
			if(persistentEntity.hasVersionProperty()) {
				
				Object updated = findById(idValue, entity.getClass());
				
				if(updated != null) {
					Object updatedVersion = getVersionPropertyValue(updated);
					
					BeanWrapper<Object> wrapper = create(entity, crateConverter.getConversionService());
					wrapper.setProperty(persistentEntity.getVersionProperty(), updatedVersion);
				}else {
					logger.info("No row found with id '{}'", idValue);
				}
			}
		}
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
		
		boolean isVersioned = isVersionPropertyDefined(entityClass);
		
		Set<String> columns = isVersioned ? persistentEntity.getPropertyNames(persistentEntity.getVersionProperty().getFieldName()) :
										    persistentEntity.getPropertyNames();
		
		T dbEntity = execute(new SelectByIdAction(tableName, idColumn, columns, id),
	   						 new DefaultSQLResponseHandler<T>(entityClass));
		
		if(dbEntity == null) {
			logger.info("No row found with id '{}'", id);
		}
		
		return dbEntity;
	}
	
	@Override
	public <T> boolean remove(Object id, Class<T> entityClass) {
		
		notNull(entityClass);
		return remove(id, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> boolean remove(Object id, Class<T> entityClass, String tableName) {
		
		notNull(entityClass);
		hasText(tableName);
		
		if(id == null) {
			return false;
		}
		
		CratePersistentProperty idProperty = getIdPropertyFor(entityClass);
		
		if (idProperty == null) {
			throw new MappingException("No id property found for object of type " + entityClass);
		}
		
		SQLResponse response = execute(new DeleteByIdAction(tableName, idProperty.getFieldName(), id));
		
		boolean removed = response.rowCount() == 1L;
		
		if(removed) {
			logger.info("Removed row with id '{}'", id);
		}else {
			logger.info("No row removed with id '{}'", id);
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
	 * @return the translated exception or the thrown exception
	 */
	private RuntimeException tryConvertingRuntimeException(RuntimeException ex) {
		RuntimeException resolved = exceptionTranslator.translateExceptionIfPossible(ex);
		return resolved == null ? ex : resolved;
	}
	
	/**
	 * Make sure the given object is not a iterable.
	 *
	 * @param entity the object to verify.
	 */
	private void ensureNotCollectionType(Object entity) {
		
		Class<?> clazz = entity.getClass();
		
		if(clazz.isArray() || ITERABLE_CLASSES.contains(clazz.getName()) ||
		   Collection.class.isAssignableFrom(clazz) || Iterable.class.isAssignableFrom(clazz) || 
		   Iterator.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Cannot use a collection type for persisting entities.");
		}
	}
	
	private boolean isIdPropertyDefined(Class<?> clazz) {
		return getPersistentEntityFor(clazz).hasIdProperty();
	}
	
	private boolean isVersionPropertyDefined(Class<?> clazz) {
		return getPersistentEntityFor(clazz).hasVersionProperty();
	}
	
	private CratePersistentProperty getIdPropertyFor(Class<?> type) {
		return getPersistentEntityFor(type).getIdProperty();
	}
	
	private CratePersistentProperty getVersionPropertyFor(Class<?> type) {
		return getPersistentEntityFor(type).getVersionProperty();
	}
	
	private Object getIdPropertyValue(Object object) {
		
		CratePersistentProperty idProperty = getIdPropertyFor(object.getClass());
		
		if(idProperty == null) {
			return null;
		}else {
			return getPropertyValue(object, idProperty);
		}
	}

	private Object getVersionPropertyValue(Object object) {
		
		CratePersistentProperty versionProperty = getVersionPropertyFor(object.getClass());
		
		if(versionProperty == null) {
			return null;
		}else {
			return getPropertyValue(object, versionProperty);
		}
	}
	
	private Object getPropertyValue(Object source, CratePersistentProperty property) {
		
		notNull(source);
		
		if(property == null) {
			return null;
		}else {
			BeanWrapper<Object> wrapper = create(source, crateConverter.getConversionService());
			return wrapper.getProperty(property);
		}
	}
	
	private void validateIdValue(Object entity) {
		
		CratePersistentProperty idProperty = getIdPropertyFor(entity.getClass());
		
		if(idProperty != null) {
			
			BeanWrapper<Object> wrapper = create(entity, crateConverter.getConversionService());
			
			Object idValue = wrapper.getProperty(idProperty);
			
			if(idValue == null) {
				throw new MappingException(PRIMARY_KEY);
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
		
		// TODO: create a generic select statement in sql package when Criteria API is in place
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
					String column = iterator.next();
					cols.append(doubleQuote(column));
					if(iterator.hasNext()) {
						cols.append(", ");
					}
				}
				
				String colNames = StringUtils.hasText(cols.toString()) ? cols.toString() : "*";
				
				statement = format("SELECT %s, %s FROM %s WHERE %s = ?", colNames, doubleQuote(RESERVED_VESRION_FIELD_NAME), 
																		 tableName, doubleQuote(idColumn));
				
				return statement;
			}
		}
	}
	
	/**
	 * @author Hasnain Javed
	 * @since 1.0.0 
	 */
	private class WholesaleUpdateByIdAction implements CrateSQLAction {
		
		private CrateSQLStatement update;
		private CrateDocument document;
		private Object idValue;

		public WholesaleUpdateByIdAction(String tableName, String idColumn, CrateDocument document, Object idValue) {
			notEmpty(document);
			this.document = document;
			this.idValue = crateConverter.convertToCrateType(idValue, null);
			this.update = new Update(idColumn, tableName, document.keySet());
		}
		
		@Override
		public SQLRequest getSQLRequest() {
			SQLRequest request = new SQLRequest(getSQLStatement(), add(document.values().toArray(), idValue));
			request.includeTypesOnResponse(true);
			return request;
		}

		@Override
		public String getSQLStatement() {
			return update.createStatement();
		}
		
		// TODO: create a generic update statement in sql package when Criteria API is in place
		private class Update extends AbstractStatement {

			private String tableName;
			private String idColumn;
			private Set<String> columns;
			
			public Update(String idColumn, String tableName, Set<String> columns) {
				
				hasText(tableName);
				hasText(idColumn);
				notEmpty(columns);
				
				this.idColumn = idColumn;
				this.tableName = tableName;
				this.columns = columns;
			}
			
			@Override
			public String createStatement() {
				
				if(StringUtils.hasText(statement)) {
					return statement;
				}
				
				StringBuilder cols = new StringBuilder();
				
				Iterator<String> iterator = columns.iterator();
				
				while(iterator.hasNext()) {
					String column = iterator.next();
					cols.append(doubleQuote(column))
						.append(" = ?");
					if(iterator.hasNext()) {
						cols.append(", ");
					}
				}
				
				statement = format("UPDATE %s set %s WHERE %s = ?", tableName, cols.toString(), doubleQuote(idColumn));
				
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
		
		// TODO: create a generic delete statement in sql package when Criteria API is in place
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