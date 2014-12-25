/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.crate.repository.support;

import java.io.Serializable;

import org.springframework.data.repository.core.EntityInformation;

/**
 * @param <T>
 * @param <ID>
 * @author Rizwan Idrees
 * @author Hasnain Javed
 * @since 1.0.0
 */
public interface CrateEntityInformation<T, ID extends Serializable> extends EntityInformation<T, ID> {

	/**
	 * Returns the name of the table the entity will be persisted to.
	 * 
	 * @return
	 */
	String getTableName();
	
	/**
	 * Returns the attribute that the id will be persisted to.
	 * 
	 * @return
	 */
	String getIdAttribute();

	/**
	 * Returns the version value from the given entity.
	 * 
	 * @return
	 */
	Long getVersion(T entity);
}