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
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang.ArrayUtils.add;
import static org.apache.commons.lang.ArrayUtils.addAll;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.CrateSQLAction.ActionType.DELETE;
import static org.springframework.data.crate.core.CrateSQLAction.ActionType.INSERT;
import static org.springframework.data.crate.core.CrateSQLAction.ActionType.SELECT;
import static org.springframework.data.crate.core.CrateSQLAction.ActionType.UPDATE;
import static org.springframework.data.crate.core.CrateSQLAction.ActionType.values;
import static org.springframework.data.crate.core.convert.CrateTypeMapper.DEFAULT_TYPE_KEY;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.INITIAL_VERSION_VALUE;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.RESERVED_VESRION_FIELD_NAME;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;
import io.crate.action.sql.SQLActionException;
import io.crate.action.sql.SQLBulkRequest;
import io.crate.action.sql.SQLBulkResponse;
import io.crate.action.sql.SQLBulkResponse.Result;
import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;
import io.crate.client.CrateClient;
import io.crate.types.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.crate.CrateSQLActionException;
import org.springframework.data.crate.core.BulkActionResult.ActionResult;
import org.springframework.data.crate.core.CrateSQLAction.ActionType;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.CrateDocumentConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.crate.core.mapping.event.AfterConvertEvent;
import org.springframework.data.crate.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.crate.core.mapping.event.AfterLoadEvent;
import org.springframework.data.crate.core.mapping.event.AfterSaveEvent;
import org.springframework.data.crate.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.crate.core.mapping.event.BeforeDeleteEvent;
import org.springframework.data.crate.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.crate.core.mapping.event.CrateMappingEvent;
import org.springframework.data.crate.core.sql.AbstractStatement;
import org.springframework.data.crate.core.sql.CrateSQLStatement;
import org.springframework.data.crate.core.sql.Insert;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.StringUtils;

/**
 * @author Hasnain Javed
 * @author Rizwan Idrees
 * @since 1.0.0
 */
public class CrateTemplate implements CrateOperations, ApplicationContextAware {

    private final Logger logger = getLogger(CrateTemplate.class);
    
	private final CrateClient client;
	private final PersistenceExceptionTranslator exceptionTranslator;
    private CrateConverter crateConverter;
    private ApplicationEventPublisher eventPublisher;
    
    private static final Collection<String> ITERABLE_CLASSES;
    private static final Collection<ActionType> ALLOWED_BULK_OPERATIONS;
    
    private static final String PRIMARY_KEY = "Primary Key must not be null";
    private static final String SQL_STATEMENT = "executing statement '{}' with args '{}'";
    private static final String NO_ID_WARNING = "Persitent Entity '{}' has no id property defined. Saving the same instance will result in a duplicate row";
    private static final String BULK_ACTION = "Invalid bulk sql action type '%s'. Allowed types are '%s'";
    private static final String ID_COLUMN = "Persistent Entity '%s' must define an id column";
    static {
        ITERABLE_CLASSES = unmodifiableCollection(asList(Collection.class.getName(),
        												 List.class.getName(),
        												 Iterator.class.getName(),
        												 Iterable.class.getName()));
        
        ALLOWED_BULK_OPERATIONS = unmodifiableCollection(asList(INSERT, UPDATE, DELETE));
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
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	this.eventPublisher = applicationContext;
	}

    @Override
    public CrateConverter getConverter() {
        return this.crateConverter;
    }

    @Override
	public SQLResponse execute(CrateAction action) throws DataAccessException {
    	return this.execute(action, new CrateActionResponseHandler<SQLResponse>() {
			@Override
			public SQLResponse handle(SQLResponse response) {
				return response;
			}
    	});
    }
    
    @Override
	public <T> T execute(CrateAction action, CrateActionResponseHandler<T> handler) throws DataAccessException {
    	
    	notNull(action, "An implementation of CrateAction is required");
    	notNull(handler, "An implementation of CrateActionResponseHandler<T> is required");
    	
    	try {
    		SQLRequest request = action.getSQLRequest();
    		logger.debug(SQL_STATEMENT, request.stmt(), Arrays.toString(request.args()));
    		return (T)handler.handle(client.sql(request).actionGet());
    	}catch(SQLActionException e) {
    		throw tryConvertingRuntimeException(e);
		}
    }
    
	@Override
	public <T> ActionableResult<T> execute(CrateBulkAction action, CrateBulkActionResponseHandler<T> handler) throws DataAccessException {
		
		notNull(action, "An implementation of CrateBulkAction is required");
		notNull(action.getActionType(), "Action Type is required");
		notNull(handler, "An implementation of CrateBulkActionResponseHandler<T> is required");
    	
		if(!ALLOWED_BULK_OPERATIONS.contains(action.getActionType())) {
			throw new CrateSQLActionException(format(BULK_ACTION, action.getActionType(),
													              ALLOWED_BULK_OPERATIONS));
		}
		
    	try {
    		SQLBulkRequest request = action.getSQLRequest();
    		if(logger.isDebugEnabled()) {
    			logger.debug(SQL_STATEMENT, request.stmt(), Arrays.deepToString(request.bulkArgs()));
    		}
    		return handler.handle(client.bulkSql(request).get());
    	}catch(SQLActionException e) {
    		throw tryConvertingRuntimeException(e);
    	}catch(InterruptedException e) {
    		throw new CrateSQLActionException(e.getMessage(), e);
		}catch(ExecutionException e) {
			throw new CrateSQLActionException(e.getMessage(), e);
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
		
		LifecycleEventCallback action = new InsertAction(entity, tableName);
		
		this.execute(action, new WriteDbCallback(action.getEntity(),
											     action.getDocument(),
											     action.getActionType()));
	}
	
	@Override
	public <T> ActionableResult<T> bulkInsert(List<T> entities, Class<T> entityClass) {
		
		notNull(entityClass);
		return bulkInsert(entities, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> ActionableResult<T> bulkInsert(List<T> entities, Class<T> entityClass, String tableName) {
		
		boolean hasId = isIdPropertyDefined(entityClass);

		if(!hasId) {
			logger.warn(NO_ID_WARNING, entityClass.getName());
		}
		
		BulkInsertOperation<T> actionHandler = new BulkInsertOperation<T>(entityClass, tableName, entities);
		
		return execute(actionHandler, actionHandler);
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
		
		LifecycleEventCallback action = new WholesaleUpdateByIdAction(entity, tableName);
		
		this.execute(action, new WriteDbCallback(action.getEntity(),
											   	 action.getDocument(),
												 action.getActionType()));
	}
	
	@Override
	public <T> ActionableResult<T> bulkUpdate(List<T> entities, Class<T> entityClass) {
		
		notNull(entityClass);
		return bulkUpdate(entities, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> ActionableResult<T> bulkUpdate(List<T> entities, Class<T> entityClass, String tableName) {
		
		notNull(entityClass);
		notEmpty(entities);
		
		BulkUpdateOperation<T> actionHandler = new BulkUpdateOperation<T>(entityClass, tableName, entities);
		
		return execute(actionHandler, actionHandler);
	}
	
	@Override
	public <T> List<T> findAll(Class<T> entityClass) {
		
		notNull(entityClass);
		return findAll(entityClass, getTableName(entityClass));
	}

	@Override
	public <T> List<T> findAll(Class<T> entityClass, String tableName) {
		
		notNull(entityClass);
		hasText(tableName);
		
		return execute(new SelectAction(entityClass, tableName, null),
					   new ActionResponseHandler<T>(entityClass));
	}
	
	@Override
	public <T> T findById(Object id, Class<T> entityClass) {
		
    	notNull(id);
		return findById(id, entityClass, getTableName(entityClass));
	}

	// TODO: re factor when the Criteria API is in place
	@Override
	public <T> T findById(Object id, Class<T> entityClass, String tableName) {
		
		notNull(id);
		
		if(!isIdPropertyDefined(entityClass)) {
			throw new MappingException(format("Entity '%s' has no id property defined", entityClass.getName()));
		}
		
		List<T> dbEntity = execute(new SelectAction(entityClass, tableName, id),
	   						 	   new ActionResponseHandler<T>(entityClass));
		if(dbEntity.isEmpty()) {
			logger.info("No row found with id '{}'", id);
			return null;
		}else {
			return dbEntity.iterator().next();
		}
	}
	
	@Override
	public <T> void deleteAll(Class<T> entityClass) {
		
		notNull(entityClass);
		deleteAll(getTableName(entityClass));
	}

	// TODO: re factor when the Criteria API is in place
	@Override
	public void deleteAll(String tableName) {
		
		hasText(tableName);
		
		final String name = tableName;
		
		execute(new CrateAction() {
			
			@Override
			public String getSQLStatement() {
				return format("DELETE FROM %s", name);
			}
			
			@Override
			public SQLRequest getSQLRequest() {
				return new SQLRequest(getSQLStatement());
			}

			@Override
			public ActionType getActionType() {
				return DELETE;
			}
		});
	}
	
	@Override
	public <T> boolean delete(Object id, Class<T> entityClass) {
		
		notNull(entityClass);
		return delete(id, entityClass, getTableName(entityClass));
	}

	@Override
	public <T> boolean delete(Object id, Class<T> entityClass, String tableName) {
		
		notNull(entityClass);
		hasText(tableName);
		
		if(id == null) {
			return false;
		}
		
		DeleteByIdActionHandler actionHandler = new DeleteByIdActionHandler(tableName, entityClass, id);
		
		return execute(actionHandler, actionHandler);
	}
	
	@Override
	public <T> ActionableResult<Object> bulkDelete(List<Object> ids, Class<T> entityClass) {
		
		notEmpty(ids);
		notNull(entityClass);
		return bulkDelete(ids, entityClass, getTableName(entityClass));
	}
	
	@Override
	public <T> ActionableResult<Object> bulkDelete(List<Object> ids, Class<T> entityClass, String tableName) {
		
		notEmpty(ids);
		notNull(entityClass);
		hasText(tableName);
		
		BulkDeleteOperation actionHandler = new BulkDeleteOperation(entityClass, tableName, ids); 
		
		return execute(actionHandler, actionHandler);
	}
	
	protected <T> void maybeEmitEvent(CrateMappingEvent<T> event) {
		if (eventPublisher != null) {
			eventPublisher.publishEvent(event);
		}
	}
	
	private String getTableName(Class<?> clazz) {
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
	
	private Object getIdPropertyValue(Object object) {
		
		CratePersistentEntity<?> entity = getPersistentEntityFor(object.getClass());
		
		if(entity.hasIdProperty()) {
			return entity.getPropertyAccessor(object).getProperty(entity.getIdProperty());
		}else {
			return null;
		}
	}
	
	private Long getVersionPropertyValue(Object object) {
		
		CratePersistentEntity<?> entity = getPersistentEntityFor(object.getClass());
		
		if(entity.hasVersionProperty()) {
			return (Long)entity.getPropertyAccessor(object).getProperty(entity.getVersionProperty());
		}else {
			return null;
		}
	}
	
	private void setVersionPropertyValue(Object object, Long versionValue) {
		
		CratePersistentEntity<?> entity = getPersistentEntityFor(object.getClass());
		
		if(entity.hasVersionProperty()) {
			entity.getPropertyAccessor(object).setProperty(entity.getVersionProperty(), versionValue);
		}
	}
	
	private void validateIdValue(Object entity) {
		
		Object idValue = getIdPropertyValue(entity);
		
		if(idValue == null) {
			throw new MappingException(PRIMARY_KEY);
		}
	}
	
	private <T> void doBeforeSave(T entity, CrateDocument document) {
		
		notNull(document);
		
		maybeEmitEvent(new BeforeConvertEvent<T>(entity));
		
		crateConverter.write(entity, document);
		
		maybeEmitEvent(new BeforeSaveEvent<Object>(entity, document));
	}
	
	private <T> void doAfterSave(T entity, CrateDocument document) {
		
		notNull(document);
		
		if(isVersionPropertyDefined(entity.getClass())) {
			setVersionPropertyValue(entity, INITIAL_VERSION_VALUE);
		}
		
		maybeEmitEvent(new AfterSaveEvent<T>(entity, document));
	}
	
	@SuppressWarnings("unchecked")
	private <T> void doAfterUpdate(T entity, CrateDocument document) {
		
		notNull(document);
		
		T dbEntity = findById(getIdPropertyValue(entity), (Class<T>)entity.getClass());
		
		if(dbEntity != null && isVersionPropertyDefined(entity.getClass())) {
			setVersionPropertyValue(entity, getVersionPropertyValue(dbEntity));
		}
		
		maybeEmitEvent(new AfterConvertEvent<T>(document, entity));
	}
	
	private void doBeforeDelete(Object id) {
		
		notNull(id);
		maybeEmitEvent(new BeforeDeleteEvent<Object>(id));
	}
	
	private void doAfterDelete(Object id) {
		
		notNull(id);
		maybeEmitEvent(new AfterDeleteEvent<Object>(id));
	}
	
	// TODO: create a generic select statement in sql package when Criteria API is in place
	private class Select extends AbstractStatement {
		
		private String idColumn;
		private String tableName;
		private Set<String> columns;
		
		public Select(String idColumn, String tableName, Set<String> columns) {
			
			hasText(tableName);

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
			
			StringBuilder sql = new StringBuilder(format("SELECT %s, %s FROM %s", colNames,
																				  doubleQuote(RESERVED_VESRION_FIELD_NAME),
																				  tableName));
			if(StringUtils.hasText(idColumn)) {
				sql.append(SPACE)
				   .append(format("WHERE %s = ?", doubleQuote(idColumn)));
			}
			
			statement = sql.toString();
			
			return statement;
		}
	}
		
	// TODO: create a generic update statement in sql package when Criteria API is in place
	private class Update extends AbstractStatement {

		private String tableName;
		private String idColumn;
		private Set<String> columns;
		
		public Update(String tableName, String idColumn, Set<String> columns) {
			
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
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class SelectAction implements CrateAction {
		
		private CrateSQLStatement select;
		private Object id;
		
		public SelectAction(Class<?> entityClass, String tableName, Object id) {
			
			notNull(entityClass);
//			notNull(id);
			
			this.id = crateConverter.convertToCrateType(id, null);
			this.select = initSelectStatement(entityClass, tableName);
		}
		
		@Override
		public SQLRequest getSQLRequest() {
			
			SQLRequest request = new SQLRequest(getSQLStatement());
			request.includeTypesOnResponse(true);
			
			if(id != null) {
				request.args(new Object[]{id});
			}
			
			return request;
		}

		@Override
		public String getSQLStatement() {
			return select.createStatement();
		}
		
		@Override
		public ActionType getActionType() {
			return SELECT;
		}
		
		private CrateSQLStatement initSelectStatement(Class<?> entityClass, String tableName) {
			
			CratePersistentEntity<?> entity = getPersistentEntityFor(entityClass);
			
			boolean isVersioned = entity.hasVersionProperty();
			
			Set<String> columns = isVersioned ? entity.getPropertyNames(entity.getVersionProperty().getFieldName()) :
												entity.getPropertyNames();
			
			String idColumn = id != null ? entity.getIdProperty().getFieldName() : null;
			
			return new Select(idColumn, tableName, columns);
		}
	}
	
	/**
	 * @author Hasnain Javed
	 * @since 1.0.0 
	 */
	private class WholesaleUpdateByIdAction implements LifecycleEventCallback {
		
		private CrateSQLStatement update;
		private CrateDocument document;
		private Object entity;
		private Object idValue;
		private CratePersistentEntity<?> persistentEntity;
		
		public WholesaleUpdateByIdAction(Object entity, String tableName) {
			
			notNull(entity);
			validateEntity(entity);
			
			this.entity = entity;
			this.persistentEntity = getPersistentEntityFor(entity.getClass());
			this.document = toDocument();
			this.idValue = crateConverter.convertToCrateType(getIdPropertyValue(entity), null);
			this.update = new Update(tableName,
									 persistentEntity.getIdProperty().getFieldName(),
									 document.keySet());
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
		
		@Override
		public ActionType getActionType() {
			return UPDATE;
		}
		
		@Override
		public Object getEntity() {
			return entity;
		}

		@Override
		public CrateDocument getDocument() {
			return document;
		}
		
		private void validateEntity(Object entity) {
			
			CratePersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
			
			if(!persistentEntity.hasIdProperty()) {
				throw new MappingException(format(ID_COLUMN, entity.getClass().getName()));
			}
			
			validateIdValue(entity);
		}
		
		private CrateDocument toDocument() {
			
			CrateDocument document = new CrateDocument();
			
			doBeforeSave(entity, document);
			
			document.remove(persistentEntity.getIdProperty().getFieldName());
			document.remove(DEFAULT_TYPE_KEY);
			
			if(persistentEntity.hasVersionProperty()) {
				document.remove(persistentEntity.getVersionProperty().getFieldName());
			}
			
			return document;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class InsertAction implements LifecycleEventCallback {
		
		private CrateSQLStatement insert;
		private CrateDocument document;
		private Object entity;
		
		public InsertAction(Object entity, String tableName) {
			
			notNull(entity);
			validateEntity(entity);
			
			this.entity = entity;
			this.document = toDocument();
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
		
		private void validateEntity(Object entity) {
			
			if(!isIdPropertyDefined(entity.getClass())) {
				logger.warn(NO_ID_WARNING, entity.getClass().getName());
			}else {
				validateIdValue(entity);
			}
		}
		
		private CrateDocument toDocument() {
			
			CrateDocument document = new CrateDocument();
			doBeforeSave(entity, document);
			
			document.remove(RESERVED_VESRION_FIELD_NAME);
			return document;
		}

		@Override
		public ActionType getActionType() {			
			return INSERT;
		}

		@Override
		public Object getEntity() {
			return entity;
		}

		@Override
		public CrateDocument getDocument() {
			return document;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class DeleteByIdActionHandler implements CrateAction, CrateActionResponseHandler<Boolean> {
		
		private CrateSQLStatement delete;
		private Object idValue;

		public DeleteByIdActionHandler(String table, Class<?> entityClass, Object id) {
			
			notNull(entityClass);
			notNull(id);
			validateEntity(entityClass);

			doBeforeDelete(id);
			this.idValue = crateConverter.convertToCrateType(id, null);
			this.delete = new Delete(table, getIdPropertyFor(entityClass).getFieldName());
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
		
		@Override
		public ActionType getActionType() {
			return DELETE;
		}
		
		@Override
		public Boolean handle(SQLResponse response) {
			
			boolean removed = response.rowCount() == 1L;
			
			if(removed) {
				logger.info("Removed row with id '{}'", idValue);
				doAfterDelete(idValue);
			}else {
				logger.info("No row removed with id '{}'", idValue);
			}
			
			return removed;
		}
		
		private void validateEntity(Class<?> entityClass) {
			
			CratePersistentProperty idProperty = getIdPropertyFor(entityClass);
			
			if (idProperty == null) {
				throw new MappingException("No id property found for object of type " + entityClass);
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	private class ActionResponseHandler<T> implements CrateActionResponseHandler<List<T>> {

		private final Class<T> type;
		
		public ActionResponseHandler(Class<T> type) {
			notNull(type);
			this.type = type;
		}
		
		@Override
		public List<T> handle(SQLResponse response) {
			
			if(response.hasRowCount()) {
				
				String[] columns = response.cols();
				DataType<?>[] types = response.columnTypes();
				Object[][] payload = response.rows();
				
				Long rows = new Long(response.rowCount());
				
				List<T> entities = new ArrayList<T>(rows.intValue());
				
				for(Object[] row : payload) {
					
					CrateDocument source = new CrateDocumentConverter(columns, types, row).toDocument();
					
					T entity = null;
						
					if(!source.isEmpty()) {
						entity = crateConverter.read(type, source);
						maybeEmitEvent(new AfterLoadEvent<T>(source, type));
					}
					
					if (entity != null) {
						maybeEmitEvent(new AfterConvertEvent<T>(source, entity));
						entities.add(entity);
					}
				}
				
				return entities;
			}else {
				return emptyList();
			}
		}
	}

	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	private class WriteDbCallback implements CrateActionResponseHandler<Void> {

		private Set<ActionType> allowedTypes = new HashSet<ActionType>(asList(INSERT, UPDATE));
		
		private ActionType actionType;
		private CrateDocument document;
		private Object entity;

		public WriteDbCallback(Object entity, CrateDocument document, ActionType actionType) {
			
			notNull(actionType);
			notNull(entity);
			notNull(document);
			
			if(!allowedTypes.contains(actionType)) {
	    		throw new CrateSQLActionException(format("Invalid sql action type '%s'. Allowed types are '%s'", actionType,
	    																										 allowedTypes));
			}
			
			this.actionType = actionType;
			this.document = document;
			this.entity = entity;
		}
		
		@Override
		public Void handle(SQLResponse response) {
			
			switch(actionType) {
			case INSERT:
				doAfterSave(entity, document);
				break;
			case UPDATE:
				Object id = getIdPropertyValue(entity);
				if(response.rowCount() > 0) {
					logger.info("Updated row with id '{}'", id);
					doAfterUpdate(entity, document);
				}else {
					logger.info("No row updated with id '{}'", id);
				}
				break;
			default:
				throw new IllegalArgumentException(format(BULK_ACTION, actionType, allowedTypes));
			}
			
			return null;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	private abstract class BaseSQLBulkOperation<T> implements CrateBulkAction, CrateBulkActionResponseHandler<T> {
		
		protected List<T> entities;		
		protected List<CrateDocument> documents;		
		protected CratePersistentEntity<?> persistentEntity;
		
		private ActionType actionType;

		public BaseSQLBulkOperation(Class<T> entityClass, List<T> entities, ActionType actionType) {
			
			notNull(entityClass);
			notNull(actionType);			
			notEmpty(entities);
			
			this.persistentEntity = getPersistentEntityFor(entityClass);
			this.actionType = actionType;
			
			// preserve order
			this.entities = new ArrayList<T>(entities);
			this.documents = new ArrayList<CrateDocument>(this.entities.size());
			
			toDocuments();
		}
		
		@Override
		public ActionType getActionType() {
			return actionType;
		}
		
		@Override
		public SQLBulkRequest getSQLRequest() {
			
			Object[][] bulkArgs = new Object[documents.size()][];
			
			for (int index = 0; index < documents.size(); index++) {
				
				Object entity = entities.get(index);
				CrateDocument document = documents.get(index);
				
				List<Object> extraArgs = appendArgs(entity);
				
				Object[] args = null;
				
				if(!extraArgs.isEmpty()) {
					args = addAll(document.values().toArray(), extraArgs.toArray());
				}else {
					args = document.values().toArray();
				}
				
				bulkArgs[index] = args;
			}
			
			SQLBulkRequest request = new SQLBulkRequest(getSQLStatement(), bulkArgs);
			
			return request;
		}
		
		@Override
		public ActionableResult<T> handle(SQLBulkResponse response) {
			
			Result[] results = response.results();
			
			BulkActionResult<T> actionResults = new BulkActionResult<T>();
			
			for(int index = 0; index < results.length; index++) {
				
				T entity = entities.get(index);
				
				CrateDocument document = documents.get(index);
				
				ActionResult<T> actionResult = actionResults.addResult(results[index], entity);
				
				if(actionResult.isSuccess()) {
					
					switch(actionType) {
					case INSERT:
						doAfterSave(entity, document);
						break;
					case UPDATE:
						doAfterUpdate(entity, document);
						break;
					default:
						throw new IllegalArgumentException(format(BULK_ACTION, actionType,
																			   Arrays.toString(values())));
					}
				}
			}
			
			return actionResults;
		}
		
		/**
		 * Converts entities to {@link CrateDocument}s and calls lifecycle callback methods. 
		 * Removes version field by default. If action type is UPDATE, the id field is removed 
		 * from the converted document as crate will throw an exception if the id (primary key) 
		 * is updated. 
		 */
		private void toDocuments() {
			
			for(T entity : entities) {
				
				CrateDocument document = new CrateDocument();
				doBeforeSave(entity, document);
				
				if(actionType == UPDATE) {
					document.remove(DEFAULT_TYPE_KEY);
					document.remove(persistentEntity.getIdProperty().getFieldName());
				}
				
				document.remove(RESERVED_VESRION_FIELD_NAME);
				
				documents.add(document);
			}
 		}
		
		/**
		 * 
		 * @param exclude the field(s) to be removed from the set. The version field if defined will be
		 * removed by default as the crate system column "_version" is readonly 
		 * @return set of fields 
		 */
		protected Set<String> getColumns(String... exclude) {
			
			String[] excludes = exclude;
			
			if(persistentEntity.hasVersionProperty()) {
				excludes = (String[])add(exclude, persistentEntity.getVersionProperty().getFieldName());
			}
			
			return isEmpty(excludes) ? persistentEntity.getPropertyNames() :
									   persistentEntity.getPropertyNames(excludes);
		}
		
		/**
		 * Custom hook for appending arguments to request payload 
		 * 
		 * @param entity the current entity being processed
		 * @return list of additional args
		 */
		protected List<Object> appendArgs(Object entity) {
			return emptyList();
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	private class BulkInsertOperation<T> extends BaseSQLBulkOperation<T> {
		
		private final CrateSQLStatement insert;
		
		public BulkInsertOperation(Class<T> entityClass, String tableName, List<T> entities) {
			
			super(entityClass, entities, INSERT);
			
			hasText(tableName);
			
			this.insert = new Insert(tableName, getColumns());
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
	 * @param <T>
	 */
	private class BulkUpdateOperation<T> extends BaseSQLBulkOperation<T> {
		
		private final CrateSQLStatement update;
		
		public BulkUpdateOperation(Class<T> entityClass, String tableName, List<T> entities) {
			
			super(entityClass, entities, UPDATE);
			
			hasText(tableName);	
			validateEntity(entityClass);
			
			this.update = new Update(tableName,
									 persistentEntity.getIdProperty().getFieldName(), 
									 getColumns(persistentEntity.getIdProperty().getFieldName()));
		}
		
		@Override
		public String getSQLStatement() {
			return update.createStatement();
		}
		
		@Override
		protected List<Object> appendArgs(Object entity) {
			return asList(getIdPropertyValue(entity));
		}
		
		private void validateEntity(Class<T> entityClass) {
			
			if(!persistentEntity.hasIdProperty()) {
				throw new MappingException(format(ID_COLUMN, entityClass.getName()));
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class BulkDeleteOperation implements CrateBulkAction, CrateBulkActionResponseHandler<Object> {

		private CrateSQLStatement delete;
		private List<Object> convertedIds;
		
		public BulkDeleteOperation(Class<?> entityClass, String tableName, List<Object> ids) {
			
			notNull(entityClass);
			notEmpty(ids);
			validateEntity(entityClass);
			
			this.delete = new Delete(tableName, getPersistentEntityFor(entityClass)
												.getIdProperty()
												.getFieldName());
			
			this.convertedIds = new ArrayList<Object>(ids.size());
			
			for(Object id : ids) {
				
				doBeforeDelete(id);
				
				Object convertedId = crateConverter.convertToCrateType(id, null);
				convertedIds.add(convertedId);
			}
		}
		
		@Override
		public ActionType getActionType() {
			return DELETE;
		}

		@Override
		public String getSQLStatement() {
			return delete.createStatement();
		}

		@Override
		public ActionableResult<Object> handle(SQLBulkResponse response) {
			
			Result[] results = response.results();
			
			BulkActionResult<Object> actionResults = new BulkActionResult<Object>();
			
			for(int index = 0; index < results.length; index++) {
				
				Object id = convertedIds.get(index);
				
				ActionResult<Object> actionResult = actionResults.addResult(results[index], id);
				
				if(actionResult.isSuccess()) {
					doAfterDelete(id);
				}
			}
			
			return actionResults;
		}
		
		@Override
		public SQLBulkRequest getSQLRequest() {
			
			Object[][] bulkArgs = new Object[convertedIds.size()][];
			
			for (int index = 0; index < bulkArgs.length; index++) {
				bulkArgs[index] = new Object[]{convertedIds.get(index)};
			}
			
			return new SQLBulkRequest(getSQLStatement(), bulkArgs);
		}
		
		public void validateEntity(Class<?> entityClass) {
			
			CratePersistentEntity<?> persistentEntity = getPersistentEntityFor(entityClass);
			
			if(!persistentEntity.hasIdProperty()) {
				throw new MappingException(format(ID_COLUMN, entityClass.getName()));
			}
		}
	}
	
	/**
	 * Call back handler adds methods to return the current entity and document for 
	 * life cycle event call backs
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private interface LifecycleEventCallback extends CrateAction {
		
		Object getEntity();
		CrateDocument getDocument();
	}
}