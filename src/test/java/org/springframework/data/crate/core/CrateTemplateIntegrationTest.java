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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.springframework.data.sample.entities.integration.EntityWithComplexId.entityWithComplexId;
import static org.springframework.data.sample.entities.integration.ObjectCollectionTypes.objectCollectionTypes;
import static org.springframework.data.sample.entities.integration.ObjectMapTypes.objectMapTypes;
import static org.springframework.data.sample.entities.integration.Person.person;
import static org.springframework.data.sample.entities.integration.SimpleCollectionTypes.simpleCollectionTypes;
import static org.springframework.data.sample.entities.integration.SimpleEntity.simpleEntity;
import static org.springframework.data.sample.entities.integration.SimpleEntityWithId.simpleEntityWithId;
import static org.springframework.data.sample.entities.integration.SimpleMapTypes.simpleMapTypes;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crate.config.TestCrateConfiguration;
import org.springframework.data.sample.entities.integration.EntityWithComplexId;
import org.springframework.data.sample.entities.integration.ObjectCollectionTypes;
import org.springframework.data.sample.entities.integration.ObjectMapTypes;
import org.springframework.data.sample.entities.integration.Person;
import org.springframework.data.sample.entities.integration.SimpleCollectionTypes;
import org.springframework.data.sample.entities.integration.SimpleEntity;
import org.springframework.data.sample.entities.integration.SimpleEntityWithId;
import org.springframework.data.sample.entities.integration.SimpleMapTypes;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Rizwan Idrees
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestCrateConfiguration.class)
public class CrateTemplateIntegrationTest {

    @Autowired
    private CrateTemplate crateTemplate;
    
    @Test
    public void shouldSaveWithoutId() {
    	
    	SimpleEntity entity = simpleEntity();
    	crateTemplate.save(entity);
    }
    
    @Test
    public void shouldSaveAndFindBySimpleId() {
    	
    	SimpleEntityWithId entity = simpleEntityWithId();
    	crateTemplate.save(entity);
    	SimpleEntityWithId dbEntity = crateTemplate.findById(entity.id, SimpleEntityWithId.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveSimpleCollectionTypesAndFindById() {
    	
    	SimpleCollectionTypes entity = simpleCollectionTypes();
    	crateTemplate.save(entity);
    	SimpleCollectionTypes dbEntity = crateTemplate.findById(entity.id, SimpleCollectionTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveObjectCollectionTypesAndFindById() {
    	
    	ObjectCollectionTypes entity = objectCollectionTypes();
    	crateTemplate.save(entity);
    	ObjectCollectionTypes dbEntity = crateTemplate.findById(entity.id, ObjectCollectionTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveSimpleMapTypesAndFindById() {
    	
    	SimpleMapTypes entity = simpleMapTypes();
    	crateTemplate.save(entity);
    	SimpleMapTypes dbEntity = crateTemplate.findById(entity.id, SimpleMapTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    	assertThat(dbEntity.mapOfBoolenWrappers.get("Key"), is(entity.mapOfBoolenWrappers.get("Key")));
    	assertThat(dbEntity.mapOfArrays.get("CRATE"), is(entity.mapOfArrays.get("CRATE")));
    	assertThat(dbEntity.mapOfCollections.get("List").toArray(), is(entity.mapOfCollections.get("List").toArray()));
    }
    
    @Test
    public void shouldSaveObjectMapTypesAndFindById() {
    	
    	ObjectMapTypes entity = objectMapTypes();
    	crateTemplate.save(entity);
    	ObjectMapTypes dbEntity = crateTemplate.findById(entity.id, ObjectMapTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    @Ignore("crate has a bug with object array nested inside another nested object")
    public void shouldSaveComplexModelAndFindById() {
    	
    	Person entity = person();
    	crateTemplate.save(entity);
    	Person dbEntity = crateTemplate.findById(entity.id, Person.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveAndFindByComplexId() {
    	
    	EntityWithComplexId entity = entityWithComplexId();
    	crateTemplate.save(entity);
    	EntityWithComplexId dbEntity = crateTemplate.findById(entity.complexId, EntityWithComplexId.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveAndRemoveBySimpleId() {
    	
    	SimpleEntityWithId entity = simpleEntityWithId();
    	entity.id = 2L;
    	crateTemplate.save(entity);
    	assertTrue(crateTemplate.removeById(entity.id, SimpleEntityWithId.class));
    }
    
    @Test
    public void shouldSaveAndRemoveByComplexId() {
    	
    	EntityWithComplexId entity = entityWithComplexId();
    	entity.complexId.booleanField = false;
    	crateTemplate.save(entity);
    	assertTrue(crateTemplate.removeById(entity.complexId, EntityWithComplexId.class));
    }
}