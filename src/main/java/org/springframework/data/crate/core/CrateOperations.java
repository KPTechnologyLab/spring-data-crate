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

import org.springframework.dao.DataAccessException;
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
    SQLResponse execute(CrateSQLAction action) throws DataAccessException;
    
    /**
     * execute the given action (insert | update | delete | alter | select)
     * @param action must not be {@literal null}.
     * @param handler must not be {@literal null}. 
     */
    <T> T execute(CrateSQLAction action, CrateSQLResponseHandler<T> handler) throws DataAccessException;
    
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
	 * Remove the given object from the table by id.
	 * 
	 * @param id the id to be used
	 * @param entityClass the type of entity
	 */
	<T> boolean remove(Object id, Class<T> entityClass);

	/**
	 * Removes the given object from the given table by id.
	 * 
	 * @param id the id to be used
	 * @param entityClass the type of entity
	 * @param table must not be {@literal null} or empty.
	 */
	<T> boolean remove(Object id, Class<T> entityClass, String tableName);
}