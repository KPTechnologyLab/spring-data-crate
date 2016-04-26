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

import static java.lang.String.format;
import static org.springframework.util.Assert.notNull;
import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.data.crate.core.BulkOperartionResult;
import org.springframework.data.crate.core.CrateAction;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.repository.CrateRepository;

/**
 * Crate specific repository implementation. Likely to be used as target within
 * {@link org.springframework.data.crate.repository.support.CrateRepositoryFactory}
 *
 * @author Rizwan Idrees
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class SimpleCrateRepository<T, ID extends Serializable> implements CrateRepository<T,ID> {

    private CrateOperations crateOperations;
    private CrateEntityInformation<T, ID> entityInformation;
    
    private Class<T> entityClass;
    
    private String tableName;

    public SimpleCrateRepository(CrateEntityInformation<T, ID> entityInformation, CrateOperations crateOperations) {
    	
        notNull(crateOperations, "CrateOperations must be configured!");
        notNull(entityInformation, "EntityInformation is missing");
        
        this.crateOperations = crateOperations;
        this.entityInformation = entityInformation;
        this.entityClass = entityInformation.getJavaType();
        this.tableName = entityInformation.getTableName();
    }

    @Override
    public <S extends T> S save(S entity) {
    	
    	notNull(entity, "Entity must not be null");
    	
    	ID id = entityInformation.getId(entity);
    	
    	if(id != null && exists(id)) {
    		crateOperations.update(entity, tableName);
    	}else {
    		crateOperations.insert(entity, tableName);
    	}
    	
        return entity;
    }
    
    @Override
    public <S extends T> List<S> save(Iterable<S> ses) {
    	
    	notNull(ses, "The given Iterable of entities must not be null");
    	
    	List<S> entities = new LinkedList<>();
    	
    	for(S entity : ses) {
    		save(entity);
    		entities.add(entity);
    	}
    	
        return entities;
    }
    
    @Override
    public T findOne(ID id) {
    	
    	notNull(id, "Id must not be null");
        return crateOperations.findById(id, entityClass, tableName);
    }

    @Override
    public boolean exists(ID id) {
    	
    	return findOne(id) == null ? false : true;
    }
    
    // TODO: re factor when the Criteria API is in place
    @Override
    public List<T> findAll() {
    	
        return crateOperations.findAll(entityClass, tableName);
    }
    
    // TODO: re factor when the Criteria API is in place and use the IN clause for ids
    @Override
    public List<T> findAll(Iterable<ID> ids) {
        
    	Iterator<ID> iterator = ids.iterator();
    	
    	Set<ID> pks = new LinkedHashSet<>();
    	
    	while(iterator.hasNext()) {
    		pks.add(iterator.next());
    	}
    	
    	List<T> entities = new ArrayList<>(pks.size());
    	
    	for(ID pk : pks) {
    		T entity = findOne(pk);
    		if(entity != null) {
    			entities.add(entity);
    		}
    	}
    	
    	return entities;
    }
    
    // TODO: re factor when the Criteria API is in place
    @Override
    public long count() {
        
    	SQLResponse response = crateOperations.execute(new CrateAction() {
			
			@Override
			public String getSQLStatement() {
				return format("SELECT count(*) from %s", tableName);
			}
			
			@Override
			public SQLRequest getSQLRequest() {
				return new SQLRequest(getSQLStatement());
			}
    	});
    	
    	Long total = 0L;
    	
    	if(response.hasRowCount()) {
    		total = (Long)response.rows()[0][0];
    	}
    	
    	return total;
    }
    
    @Override
    public void delete(ID id) {
    	
    	notNull(id, "The given id must not be null");
    	crateOperations.delete(id, entityClass, tableName);
    }

    @Override
    public void delete(T entity) {
    	
    	notNull(entity, "Entity must not be null");
    	delete(entityInformation.getId(entity));
    }

    // TODO: re factor when the Criteria API is in place and use the IN clause for entity ids
    @Override
    public void delete(Iterable<? extends T> ts) {
        
    	Iterator<? extends T> iterator = ts.iterator();
    	
    	while(iterator.hasNext()) {
    		delete(entityInformation.getId(iterator.next()));
    	}
    }

    @Override
    public void deleteAll() {    	
    	crateOperations.deleteAll(tableName);
    }
    
    @Override
	public BulkOperartionResult<T> bulkInsert(List<T> entities) {
		
    	notNull(entities, "The given List of entities must not be null");
		return crateOperations.bulkInsert(entities, entityClass, tableName);
	}
    
	@Override
	public BulkOperartionResult<T> bulkUpdate(List<T> entities) {
		
    	notNull(entities, "The given List of entities must not be null");
		return crateOperations.bulkUpdate(entities, entityClass, tableName);
	}
	
	@Override
	public BulkOperartionResult<Object> bulkDelete(List<Object> ids) {
		
    	notNull(ids, "The given List of Ids must not be null");
		return crateOperations.bulkDelete(ids, entityClass, tableName);
	}
	
	@Override
	public void refreshTable() {
		crateOperations.refreshTable(tableName);
	}
	
    /**
	 * Returns the underlying {@link CrateOperations} instance.
	 * 
	 * @return
	 */
	protected CrateOperations getCrateOperations() {
		return this.crateOperations;
	}

	/**
	 * @return the entityInformation
	 */
	protected CrateEntityInformation<T, ID> getEntityInformation() {
		return entityInformation;
	}
}