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
}