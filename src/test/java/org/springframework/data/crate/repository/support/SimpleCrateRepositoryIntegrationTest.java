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

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.crate.config.AbstractCrateConfiguration;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;
import org.springframework.data.crate.repository.CrateRepository;
import org.springframework.data.crate.repository.support.SimpleCrateRepositoryIntegrationTest.CrateContextConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Rizwan Idress
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CrateContextConfig.class})
public class SimpleCrateRepositoryIntegrationTest {
	
	@Autowired
	private CrateOperations crateOperations;
	
	private CrateEntityInformation<Person, String> entityInformation = new PersonInformation();
	
	private CrateRepository<Person, String> repository;
	
	private List<Person> people;
	
	private Person hasnain;
	private Person rizwan;
	
	/**
	 *  TODO: remove sleep method once the @Table annotation is enhanced
	 *  to turn off replicas with number_of_replicas table property set to 0
	 * @throws InterruptedException
	 */	
	@Before
	@SuppressWarnings("static-access")
	public void setup() throws InterruptedException {
		
		repository = new SimpleCrateRepository<Person, String>(entityInformation, crateOperations);
		
		hasnain = new Person("hasnain@test.com", "Hasnain Javed", 34);
		rizwan = new Person("rizwan@test.com", "Rizwan Idress", 33);
		
		people = asList(rizwan, hasnain);
		
		repository.save(people);
		
		currentThread().sleep(1000);
	}

	@After
	public void teardown() throws InterruptedException {
		repository.deleteAll();
	}
	
	@Test
	public void shouldFindOne() {
		
		Person person = people.iterator().next();
		Person dbPerson = repository.findOne(person.getEmail());
		
		assertThat(dbPerson, is(notNullValue()));
		assertThat(dbPerson.getEmail(), is(person.getEmail()));
	}
	
	@Test
	public void shouldExist() {
		assertTrue(repository.exists(people.iterator().next().getEmail()));
	}
	
	@Test
	public void shouldFindAll() throws InterruptedException {
		assertThat(people.size(), is(repository.findAll().size()));
	}
	
	@Test
	public void shouldFindAllById() throws InterruptedException {
		assertThat(people.size(), is(repository.findAll(asList(hasnain.getEmail(), rizwan.getEmail())).size()));
	}
	
	@Test
	public void shouldCount() throws InterruptedException {
		assertThat((long)people.size(), is(repository.count()));
	}
	
	@Test
	public void shouldDelete() throws InterruptedException {
		repository.delete(hasnain);
		assertThat(repository.findOne(hasnain.getEmail()), is(nullValue()));
	}
	
	@Table(name="person")
	static class Person {
		
		@Id
		private String email;
		private String name;
		private int age;
		
		@Version
		private long version;
		
		public Person(String email, String name, int age) {
			super();
			this.email = email;
			this.name = name;
			this.age = age;
		}
		
		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getAge() {
			return age;
		}
		
		public void setAge(int age) {
			this.age = age;
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}

		@Override
		public boolean equals(Object obj) {
			
			if (!(obj instanceof Person)) {
	            return false;
	        }
			
	        if (this == obj) {
	            return true;
	        }
	        
	        Person that = (Person) obj;
	        
	        return new EqualsBuilder().append(this.email, that.email)
	        						  .append(this.name, that.name)
	        						  .append(this.age, that.age)
	        						  .isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder(11, 21).append(email)
											  .append(name)
											  .append(age)
											  .toHashCode();
		}
	}
	
	@Configuration
	static class CrateContextConfig extends AbstractCrateConfiguration {
		
		@Bean
		public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
			return new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP);
		}
	}
	
	private static class PersonInformation implements CrateEntityInformation<Person, String> {

		@Override
		public boolean isNew(Person entity) {
			return entity.getEmail() == null;
		}

		@Override
		public String getId(Person entity) {
			return entity.getEmail();
		}

		@Override
		public Class<String> getIdType() {
			return String.class;
		}

		@Override
		public Class<Person> getJavaType() {
			return Person.class;
		}

		@Override
		public String getTableName() {
			return Person.class.getSimpleName().toLowerCase();
		}

		@Override
		public String getIdAttribute() {
			return "email";
		}

		@Override
		public Long getVersion(Person entity) {
			return entity.getVersion();
		}
	}
}