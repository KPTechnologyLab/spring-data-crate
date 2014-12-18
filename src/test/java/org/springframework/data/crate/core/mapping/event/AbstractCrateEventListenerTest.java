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
@ContextConfiguration(classes={EventContextConfiguration.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class AbstractCrateEventListenerTest {

	@Autowired
	private CrateOperations crateOperations;

	@Autowired
	private SimpleMappingEventListener eventListener;
	
	@After
	public void teardown() {
		eventListener.clearEvents();
	}
	
	@Test
	public void shouldEmitEventsOnInsert() {
		
		insertUser("1@test.com", "hasnain javed", 34);
		
		assertEquals(1, eventListener.onBeforeSaveEvents.size());
		assertEquals(1, eventListener.onAfterSaveEvents.size());
		assertEquals(1, eventListener.onBeforeConvertEvents.size());
	}
	
	@Test
	public void shouldEmitEventsOnUpdate() {
		
		User user = insertUser("2@test.com", "hasnain javed", 34);
		user.setAge(35);
		
		crateOperations.update(user);
		
		assertEquals(2, eventListener.onBeforeSaveEvents.size()); // once for insert and once for update
		assertEquals(1, eventListener.onAfterSaveEvents.size());
		assertEquals(2, eventListener.onBeforeConvertEvents.size()); // once for insert and once for update
	}
	
	@Test
	public void shouldEmitEventsOnRemove() {
		
		User user = insertUser("3@test.com", "hasnain javed", 34);

		crateOperations.remove(user.getId(), User.class);
		
		assertEquals(1, eventListener.onBeforeDeleteEvents.size());
		assertEquals(1, eventListener.onAfterDeleteEvents.size());
	}
	
	@Test
	public void shouldEmitEventsOnFindById() {
		
		User user = insertUser("4@test.com", "hasnain javed", 34);
		
		crateOperations.findById(user.getId(), User.class);

		assertEquals(1, eventListener.onAfterLoadEvents.size());
		assertEquals(1, eventListener.onAfterConvertEvents.size());
	}
	
	private User insertUser(String id, String name, int age) {
		User user = new User(id, name, age);
		crateOperations.insert(user);
		return user;
	}
}