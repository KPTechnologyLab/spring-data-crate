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

import io.crate.action.sql.SQLResponse;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.crate.CrateSQLActionException;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.mapping.SimpleCratePersistentEntity;

/**
 * 
 * @author Hasnain Javed
 * @author Rizwan Idrees
 *
 * @since 1.0.0
 */
public interface CrateOperations {

    /**
     * Get the crate converter in use
     * @return CrateConverter
     */
    CrateConverter getConverter();
    
    /**
     * execute the given action (insert | update | delete | alter | select)
     * @param action must not be {@literal null}.
     * @return response returned by crate as a result of executing the specified action
     */
    SQLResponse execute(CrateAction action) throws DataAccessException;
    
    /**
     * execute the given action (insert | update | delete | alter | select)
     * @param action must not be {@literal null}.
     * @param handler must not be {@literal null}. 
     */
    <T> T execute(CrateAction action, CrateActionResponseHandler<T> handler) throws DataAccessException;
    
    /**
     * Execute the given bulk operation (insert | update | delete)
     * All operations are executed whatsoever. It does not matter whether one single operation failed or all succeeded
     * @param action must not be {@literal null}.
     * @param handler must not be {@literal null}. 
     */
    <T> BulkActionResult<T> execute(CrateBulkAction action, CrateBulkActionResponseHandler<T> handler) throws DataAccessException, CrateSQLActionException;
    
    /**
     * Insert the given object. If the object defines an id (primary key), it must not be null.
     * The Table name will be determined by the backing {@link SimpleCratePersistentEntity} instance.
     *
     * @param entity the object to store in the table.
     */
    void insert(Object entity);
    
    /**
     * Insert the given object. If the object defines an id (primary key), it must not be null. 
     *
     * @param entity the object to store in the table.
     * @param tableName name of the table to store the object in
     */
    void insert(Object entity, String tableName);
    
    /**
     * Insert the given list of objects. If the object defines an id (primary key), it must not be null.
     * All life cycle callback methods will be invoked for entities succeeding the insert operation.
     *
     * @param entities the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @return Results containing the details of the bulk operation
     */
    <T> BulkActionResult<T> bulkInsert(List<T> entities, Class<T> entityClass);
    
    /**
     * Insert the given list of objects in the given table. If the object defines an id (primary key), it must not be null.
     * All life cycle callback methods will be invoked for entities succeeding the insert operation.
     * 
     * @param entities the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @param tableName name of the table to store the object in.
     * @return Results containing the details of the bulk operation
     */
    <T> BulkActionResult<T> bulkInsert(List<T> entities, Class<T> entityClass, String tableName);
    
    /**
     * Update the given object. The object must define an id (primary key) and the value must not be null. 
     * The Table name will be determined by the backing {@link SimpleCratePersistentEntity} instance.
     * The columns being updated must not have been used to partition the table using the PARTITIONED BY clause
     * and will result in an exception. 
     * By default, the primary key is used as the routing column if defined, otherwise the internal _id column
     * is used as the routing column. The primary key and the version properties if defined will be excluded from
     * the update query even if they are changed by the calling code and will not be reflected in the database. 
     * @param entity the object to store in the table.
     * @throws {@link InvalidDataAccessResourceUsageException}
     */
    void update(Object entity);
    
    /**
     * Update the given object. The object must define an id (primary key). 
     * The Table name will be determined by the backing {@link SimpleCratePersistentEntity} instance.
     * The columns being updated must not have been used to partition the table using the PARTITIONED BY clause
     * and will result in an exception. 
     * By default, the primary key is used as the routing column if defined, otherwise the internal _id column
     * is used as the routing column. The primary key and the version properties if defined will be excluded from
     * the update query even if they are changed by the calling code and will not be reflected in the database.
     * @param entity the object to store in the table.
     * @param tableName name of the table to store the object in
     * @throws {@link InvalidDataAccessResourceUsageException}
     */
    void update(Object entity, String tableName);
    
    /**
     * Update the given list of objects. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for entities succeeding the update operation.
     *
     * @param entities the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @return Results containing the details of the bulk operation
     */
    <T> BulkActionResult<T> bulkUpdate(List<T> entities, Class<T> entityClass);
    
    /**
     * Update the given list of objects in the given table. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for entities succeeding the update operation.
     * 
     * @param entities the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @param tableName name of the table to store the object in.
     * @return Results containing the details of the bulk operation
     */
    <T> BulkActionResult<T> bulkUpdate(List<T> entities, Class<T> entityClass, String tableName);
    
    /**
	 * Query for a list of objects of type T from the table used by the entity class.
	 * <p/>
	 * The object is converted from the Crate native representation using an instance of {@see CrateDocumentConverter}
	 * and {@see CrateConverter}. Unless configured otherwise, an instance of MappingCrateConverter will be used.
	 * <p/>
	 * 
	 * @param entityClass the parameterized type of the returned list
	 * @return the converted collection
	 */
	<T> List<T> findAll(Class<T> entityClass);

	/**
	 * Query for a list of objects of type T from the table used by the entity class.
	 * <p/>
	 * The object is converted from the Crate native representation using an instance of {@see CrateDocumentConverter}
	 * and {@see CrateConverter}. Unless configured otherwise, an instance of MappingCrateConverter will be used.
	 * <p/>
	 * 
	 * @param entityClass the parameterized type of the returned list.
	 * @param tableName name of the table to retrieve the objects from
	 * @return the converted collection
	 */
	<T> List<T> findAll(Class<T> entityClass, String tableName);
    
    /**
	 * Returns a document with the given id mapped onto the given target class. The table the query is ran against will be
	 * derived from the given target class as well.
	 * 
	 * @param <T>
	 * @param id the id of the document to return.
	 * @param entityClass the type the document should be converted to.
	 * @return the document with the given id mapped onto the given target class.
	 */
    <T> T findById(Object id, Class<T> entityClass);
    
    /**
	 * Returns the document with the given id from the given table name mapped onto the given target class.
	 * 
	 * @param id the id of the document to return
	 * @param entityClass the type to convert the document to
	 * @param tableName the table to query for the document
	 * @param <T>
	 * @return
	 */
	<T> T findById(Object id, Class<T> entityClass, String tableName);
	
	/**
	 * Remove all rows from the table used by the entity class.
	 * 
	 * @param entityClass the type of entity
	 */
	<T> void deleteAll(Class<T> entityClass);
	
	/**
	 * Remove all rows from the table.
	 * 
	 * @param tableName name of the table to remove from
	 */
	void deleteAll(String tableName);
	
	/**
	 * Remove the given object from the table by id.
	 * 
	 * @param id the id to be used
	 * @param entityClass the type of entity
	 */
	<T> boolean delete(Object id, Class<T> entityClass);

	/**
	 * Removes the given object from the given table by id.
	 * 
	 * @param id the id to be used
	 * @param entityClass the type of entity
	 * @param table must not be {@literal null} or empty.
	 */
	<T> boolean delete(Object id, Class<T> entityClass, String tableName);
	
	/**
     * Delete the given list of objects. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for entities succeeding the delete operation.
     *
     * @param ids the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @return Results containing the details of the bulk operation
     */
	<T> BulkActionResult<Object> bulkDelete(List<Object> ids, Class<T> entityClass);
    
    /**
     * Delete the given list of objects from the given table. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for entities succeeding the delete operation.
     * 
     * @param ids the list of objects to store in the table.
     * @param entityClass the parameterized type of the object.
     * @param tableName name of the table to delete the object from.
     * @return Results containing the details of the bulk operation
     */
    <T> BulkActionResult<Object> bulkDelete(List<Object> ids, Class<T> entityClass, String tableName);
}