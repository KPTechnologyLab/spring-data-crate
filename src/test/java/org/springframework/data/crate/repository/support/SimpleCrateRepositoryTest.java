/*
 * Copyright 2015 the original author or authors.
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.repository.CrateRepository;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleCrateRepositoryTest {
	
	@Mock
	private CrateOperations crateOperations;
	
	private CrateEntityInformation<EntityWithId, String> entityWithIdInformation = new EntityWithIdInformation();
	private CrateEntityInformation<EntityWithOutId, String> entityWithoutIdInformation = new EntityWithoutIdInformation();
	
	private CrateRepository<EntityWithId, String> entityWithIdRepository;
	private CrateRepository<EntityWithOutId, String> entityWithoutIdRepository;
	
	@Before
	public void setup() {
		entityWithIdRepository = new SimpleCrateRepository<EntityWithId, String>(entityWithIdInformation, crateOperations);
		entityWithoutIdRepository = new SimpleCrateRepository<EntityWithOutId, String>(entityWithoutIdInformation, crateOperations);
	}
	
	@Test
	public void shouldSaveEntityWithId() {
		
		EntityWithId entity = new EntityWithId("hasnain@test.com", "Hasnain");
		
		when(crateOperations.findById(entity.getEmail(), EntityWithId.class,"entitywithid")).thenReturn(null);
		
		entityWithIdRepository.save(entity);
		
		verify(crateOperations).findById(entity.getEmail(), EntityWithId.class, "entitywithid");
		verify(crateOperations).insert(any(EntityWithId.class), eq("entitywithid"));
		verify(crateOperations, never()).update(any(EntityWithId.class), anyString());
	}
	
	@Test
	public void shouldUpdateEntityWithId() {
			
		EntityWithId entity = new EntityWithId("hasnain@test.com", "Hasnain");
		
		when(crateOperations.findById(entity.getEmail(), EntityWithId.class,"entitywithid")).thenReturn(entity);
		
		entityWithIdRepository.save(entity);
		
		verify(crateOperations).findById(entity.getEmail(), EntityWithId.class, "entitywithid");
		verify(crateOperations).update(any(EntityWithId.class), eq("entitywithid"));
		verify(crateOperations, never()).insert(any(EntityWithId.class), anyString());
	}
	
	@Test
	public void shouldSaveEntityWithNoId() {
		
		EntityWithOutId entity = new EntityWithOutId("hasnain@test.com", "Hasnain");
		
		entityWithoutIdRepository.save(entity);
		
		verify(crateOperations).insert(any(EntityWithOutId.class), eq("entitywithoutid"));
		verify(crateOperations, never()).findById(anyObject(), eq(EntityWithOutId.class), anyString());
		verify(crateOperations, never()).update(any(EntityWithOutId.class), anyString());
	}
	
	static class EntityWithId {
		
		@Id
		private String email;
		private String name;
		
		public EntityWithId(String email, String name) {
			super();
			this.email = email;
			this.name = name;
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
	}
	
	static class EntityWithOutId {
		
		private String email;
		private String name;
		
		public EntityWithOutId(String email, String name) {
			super();
			this.email = email;
			this.name = name;
		}
	}
	
	private static class EntityWithIdInformation implements CrateEntityInformation<EntityWithId, String> {

		@Override
		public boolean isNew(EntityWithId entity) {
			return entity.getEmail() == null;
		}

		@Override
		public String getId(EntityWithId entity) {
			return entity.getEmail();
		}

		@Override
		public Class<String> getIdType() {
			return String.class;
		}

		@Override
		public Class<EntityWithId> getJavaType() {
			return EntityWithId.class;
		}

		@Override
		public String getTableName() {
			return EntityWithId.class.getSimpleName().toLowerCase();
		}

		@Override
		public String getIdAttribute() {
			return "email";
		}

		@Override
		public Long getVersion(EntityWithId entity) {
			return null;
		}
	}
	
	private static class EntityWithoutIdInformation implements CrateEntityInformation<EntityWithOutId, String> {

		@Override
		public boolean isNew(EntityWithOutId entity) {
			return true;
		}

		@Override
		public String getId(EntityWithOutId entity) {
			return null;
		}

		@Override
		public Class<String> getIdType() {
			return String.class;
		}

		@Override
		public Class<EntityWithOutId> getJavaType() {
			return EntityWithOutId.class;
		}

		@Override
		public String getTableName() {
			return EntityWithOutId.class.getSimpleName().toLowerCase();
		}

		@Override
		public String getIdAttribute() {
			return null;
		}

		@Override
		public Long getVersion(EntityWithOutId entity) {
			return null;
		}
	}
}