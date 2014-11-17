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
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;
import io.crate.action.sql.SQLActionException;
import io.crate.action.sql.SQLResponse;
import io.crate.client.CrateClient;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.crate.CrateSQLActionException;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;

/**
 * @author Hasnain Javed
 * @author Rizwan Idrees
 *
 * @since 1.0.0
 */
public class CrateTemplate implements CrateOperations {

    private final Logger logger = getLogger(CrateTemplate.class);
    
	private final CrateClient client;
	private final PersistenceExceptionTranslator exceptionTranslator;
    private CrateConverter crateConverter;
	
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
    		return (T)handler.handle(client.sql(action.getSQLRequest()).actionGet());
    	}catch(SQLActionException e) {
    		throw tryConvertingRuntimeException(e);
		}
    }
    
    private CratePersistentEntity<?> getPersistentEntityFor(Class<?> clazz) {
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
}