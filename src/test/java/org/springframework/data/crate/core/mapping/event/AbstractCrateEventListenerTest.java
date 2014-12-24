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
package org.springframework.data.crate.core.mapping.event;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={LifecycleEventConfiguration.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class AbstractCrateEventListenerTest {

	@Autowired
	private CrateOperations crateOperations;

	@Autowired
	private SimpleMappingEventListener eventListener;
	
	@Before
	public void setup() {
		crateOperations.deleteAll(User.class);
	}
	
	@After
	public void teardown() {
		eventListener.clearEvents();
		crateOperations.deleteAll(User.class);
	}
	
	@Test
	public void shouldEmitEventsOnInsert() {
		
		insertUser();
		
		assertEquals(1, eventListener.onBeforeConvertEvents.size());
		assertEquals(1, eventListener.onBeforeSaveEvents.size());
		assertEquals(1, eventListener.onAfterSaveEvents.size());
	}
	
	@Test
	public void shouldEmitEventsOnUpdate() {
		
		User user = insertUser();
		user.setAge(35);
		
		crateOperations.update(user);
		
		assertEquals(2, eventListener.onBeforeConvertEvents.size()); // once for insert and once for update
		assertEquals(2, eventListener.onBeforeSaveEvents.size()); // once for insert and once for update
		assertEquals(1, eventListener.onAfterSaveEvents.size());
	}
	
	@Test
	public void shouldEmitEventsOnRemove() {
		
		User user = insertUser();;

		crateOperations.delete(user.getId(), User.class);
		
		assertEquals(1, eventListener.onBeforeDeleteEvents.size());
		assertEquals(1, eventListener.onAfterDeleteEvents.size());
	}
	
	@Test
	public void shouldEmitEventsOnFindById() {
		
		User user = insertUser();
		
		crateOperations.findById(user.getId(), User.class);

		assertEquals(1, eventListener.onAfterLoadEvents.size());
		assertEquals(1, eventListener.onAfterConvertEvents.size());
	}
	
	private User insertUser() {
		User user = new User("1@test.com", "hasnain javed", 34);
		crateOperations.insert(user);
		return user;
	}
}