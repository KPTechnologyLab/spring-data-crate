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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.sample.entities.integration.EntityWithComplexId.entityWithComplexId;
import static org.springframework.data.sample.entities.integration.EntityWithNesting.entityWithNestingAndSimpleId;
import static org.springframework.data.sample.entities.integration.ObjectCollectionTypes.objectCollectionTypes;
import static org.springframework.data.sample.entities.integration.ObjectMapTypes.objectMapTypes;
import static org.springframework.data.sample.entities.integration.Person.person;
import static org.springframework.data.sample.entities.integration.SimpleCollectionTypes.simpleCollectionTypes;
import static org.springframework.data.sample.entities.integration.SimpleEntity.simpleEntity;
import static org.springframework.data.sample.entities.integration.SimpleEntityWithId.simpleEntityWithId;
import static org.springframework.data.sample.entities.integration.SimpleMapTypes.simpleMapTypes;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crate.config.TestCrateConfiguration;
import org.springframework.data.sample.entities.integration.EntityWithComplexId;
import org.springframework.data.sample.entities.integration.EntityWithNesting;
import org.springframework.data.sample.entities.integration.Language;
import org.springframework.data.sample.entities.integration.ObjectCollectionTypes;
import org.springframework.data.sample.entities.integration.ObjectMapTypes;
import org.springframework.data.sample.entities.integration.Person;
import org.springframework.data.sample.entities.integration.SimpleCollectionTypes;
import org.springframework.data.sample.entities.integration.SimpleEntity;
import org.springframework.data.sample.entities.integration.SimpleEntityWithId;
import org.springframework.data.sample.entities.integration.SimpleMapTypes;
import org.springframework.data.sample.entities.integration.User;
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
    
    @Before
    public void setup() {
    	
    	crateTemplate.deleteAll(SimpleEntity.class);
    	crateTemplate.deleteAll(SimpleEntityWithId.class);
    	crateTemplate.deleteAll(SimpleCollectionTypes.class);
    	crateTemplate.deleteAll(ObjectCollectionTypes.class);
    	crateTemplate.deleteAll(SimpleMapTypes.class);
    	crateTemplate.deleteAll(ObjectMapTypes.class);
    	crateTemplate.deleteAll(Person.class);
    	crateTemplate.deleteAll(EntityWithComplexId.class);
    	crateTemplate.deleteAll(EntityWithNesting.class);
    	crateTemplate.deleteAll(User.class);
    }
    
    @After
    public void teardown() {
    	
    	crateTemplate.deleteAll(SimpleEntity.class);
    	crateTemplate.deleteAll(SimpleEntityWithId.class);
    	crateTemplate.deleteAll(SimpleCollectionTypes.class);
    	crateTemplate.deleteAll(ObjectCollectionTypes.class);
    	crateTemplate.deleteAll(SimpleMapTypes.class);
    	crateTemplate.deleteAll(ObjectMapTypes.class);
    	crateTemplate.deleteAll(Person.class);
    	crateTemplate.deleteAll(EntityWithComplexId.class);
    	crateTemplate.deleteAll(EntityWithNesting.class);
    	crateTemplate.deleteAll(User.class);
    }
    
    @Test
    public void shouldSaveWithoutIdAndInitialVersion() {
    	
    	SimpleEntity entity = simpleEntity();
    	crateTemplate.insert(entity);
    	assertThat(entity.version, is(notNullValue()));
    	assertThat(entity.version, is(1L));
    }
    
    @Test
    public void shouldSaveWithInitialVersionAndFindBySimpleId() {
    	
    	SimpleEntityWithId entity = simpleEntityWithId();
    	crateTemplate.insert(entity);
    	assertThat(entity.version, is(1L));
    	SimpleEntityWithId dbEntity = crateTemplate.findById(entity.id, SimpleEntityWithId.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity.version, is(notNullValue()));
    	assertThat(dbEntity.version, is(1L));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveSimpleCollectionTypesAndFindById() {
    	
    	SimpleCollectionTypes entity = simpleCollectionTypes();
    	crateTemplate.insert(entity);
    	SimpleCollectionTypes dbEntity = crateTemplate.findById(entity.id, SimpleCollectionTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveObjectCollectionTypesAndFindById() {
    	
    	ObjectCollectionTypes entity = objectCollectionTypes();
    	crateTemplate.insert(entity);
    	ObjectCollectionTypes dbEntity = crateTemplate.findById(entity.id, ObjectCollectionTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveSimpleMapTypesAndFindById() {
    	
    	SimpleMapTypes entity = simpleMapTypes();
    	crateTemplate.insert(entity);
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
    	crateTemplate.insert(entity);
    	ObjectMapTypes dbEntity = crateTemplate.findById(entity.id, ObjectMapTypes.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveComplexModelAndFindById() {
    	
    	Person entity = person();
    	crateTemplate.insert(entity);
    	Person dbEntity = crateTemplate.findById(entity.id, Person.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveAndFindByComplexId() {
    	
    	EntityWithComplexId entity = entityWithComplexId();
    	crateTemplate.insert(entity);
    	EntityWithComplexId dbEntity = crateTemplate.findById(entity.complexId, EntityWithComplexId.class);
    	assertThat(dbEntity, is(notNullValue()));
    	assertThat(dbEntity, is(entity));
    }
    
    @Test
    public void shouldSaveAndRemoveBySimpleId() {
    	
    	SimpleEntityWithId entity = simpleEntityWithId();
    	entity.id = 2L;
    	crateTemplate.insert(entity);
    	assertTrue(crateTemplate.delete(entity.id, SimpleEntityWithId.class));
    }
    
    @Test
    public void shouldSaveAndRemoveByComplexId() {
    	
    	EntityWithComplexId entity = entityWithComplexId();
    	entity.complexId.booleanField = false;
    	crateTemplate.insert(entity);
    	assertTrue(crateTemplate.delete(entity.complexId, EntityWithComplexId.class));
    }
    
    @Test
    public void shouldSaveAndUpdateEntityAndBumpUpVersion() {
    	
    	Language language = new Language();
    	language.name = "Groovy";
    	
    	EntityWithNesting entity = entityWithNestingAndSimpleId();
    	crateTemplate.insert(entity);

    	entity.name = "CrateDB";
    	entity.country.languages.add(language);
    	entity.country.name = "Canada";
    	entity.map.put("Key_2", "Value_2");
    	entity.integers.add(3);
    	
    	crateTemplate.update(entity);
    	
    	EntityWithNesting dbEntity = crateTemplate.findById(entity.id, EntityWithNesting.class);
    	
    	assertThat(dbEntity, is(entity));
    	assertThat(dbEntity.version, is(2L));
    }
    
    @Test
    public void shouldNotUpdateWhenIdIsChanged() {
    	
    	Language language = new Language();
    	language.name = "Groovy";
    	
    	EntityWithNesting entity = entityWithNestingAndSimpleId();
    	crateTemplate.insert(entity);

    	entity.id = 2L;
    	entity.name = "CrateDB";
    	entity.country.languages.add(language);
    	entity.country.name = "Canada";
    	entity.map.put("Key_2", "Value_2");
    	entity.integers.add(3);
    	
    	crateTemplate.update(entity);
    	
    	EntityWithNesting dbEntity = crateTemplate.findById(entity.id, EntityWithNesting.class);
    	
    	assertThat(dbEntity, is(nullValue()));
    }
    
    @Test
    public void shouldBulkInsert() {
    	
    	User hasnain = new User("hasnain@test.com", "Hasnain Javed", 34);
    	User rizwan = new User("rizwan@test.com", "Rizwan Idrees", 33);
    	
    	BulkActionResult<User> results = crateTemplate.bulkInsert(asList(hasnain, rizwan), User.class);
    	
    	assertThat(results.getAllSuccesses().size(), is(2));
    	assertThat(results.getAllFailures().isEmpty(), is(true));
    }
    
    @Test
    public void shouldBulkUpdate() {
    	
    	User hasnain = new User("hasnain@test.com", "Hasnain Javed", 34);
    	User rizwan = new User("rizwan@test.com", "Rizwan Idrees", 33);
    	
    	crateTemplate.bulkInsert(asList(hasnain, rizwan), User.class);
    	
    	hasnain.setName("Hasnain");
    	rizwan.setName("Rizwan");
    	
    	BulkActionResult<User> results = crateTemplate.bulkUpdate(asList(hasnain, rizwan), User.class);
    	
    	assertThat(results.getAllSuccesses().size(), is(2));
    	assertThat(results.getAllFailures().isEmpty(), is(true));
    }
    
    @Test
    public void shouldBulkDelete() {
    	
    	User hasnain = new User("hasnain@test.com", "Hasnain Javed", 34);
    	User rizwan = new User("rizwan@test.com", "Rizwan Idrees", 33);
    	
    	crateTemplate.bulkInsert(asList(hasnain, rizwan), User.class);
    	
    	List<Object> ids = new ArrayList<Object>(asList(hasnain.getId(), rizwan.getId()));
    	
    	BulkActionResult<Object> results = crateTemplate.bulkDelete(ids, User.class);
    	
    	assertThat(results.getAllSuccesses().size(), is(2));
    	assertThat(results.getAllFailures().isEmpty(), is(true));
    }
}