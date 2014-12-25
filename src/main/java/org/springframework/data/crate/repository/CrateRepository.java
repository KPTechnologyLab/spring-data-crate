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
package org.springframework.data.crate.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.crate.core.ActionableResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @param <T>
 * @param <ID>
 * @author Rizwan Idrees
 * @author Hasnain Javed
 * @since 1.0.0
 */

// TODO: extend PagingAndSortigRepository once the criteria api is in place
@NoRepositoryBean
public interface CrateRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
	 */
	<S extends T> List<S> save(Iterable<S> entites);
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findAll()
	 */
	List<T> findAll();
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
	 */
	List<T> findAll(Iterable<ID> ids);
	
	/**
     * Insert the given list of objects. If the object defines an id (primary key), it must not be null.
     * All life cycle callback methods will be invoked for entities succeeding the insert operation.
     *
     * @param entities the list of objects to store in the table.
     * @return Results containing the details of the bulk operation
     */
    ActionableResult<T> bulkInsert(List<T> entities);
    
    /**
     * Update the given list of objects. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for entities succeeding the update operation.
     *
     * @param entities the list of objects to store in the table.
     * @return Results containing the details of the bulk operation
     */
    ActionableResult<T> bulkUpdate(List<T> entities);
    
    /**
     * Delete the given list of objects. The object must define an id (primary key).
     * All life cycle callback methods will be invoked for an id succeeding the delete operation.
     *
     * @param ids the list of object ids.
     * @return Results containing the details of the bulk operation
     */
    ActionableResult<Object> bulkDelete(List<Object> ids);
}