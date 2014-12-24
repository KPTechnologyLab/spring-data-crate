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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={LifecycleEventConfiguration.class})
public class ValidatingCrateEventListenerTest {

	@Autowired
	CrateOperations crateOperations;

	@Test
	public void shouldThrowConstraintViolationException() {
		
		User user = new User("1@test.com", "hasnain", 10);

		try {
			crateOperations.insert(user);
			fail();
		} catch (ConstraintViolationException e) {
			assertThat(e.getConstraintViolations().size(), equalTo(2));
		}
	}

	@Test
	public void shouldNotThrowAnyExceptions() {
		crateOperations.insert(new User("test@test.com", "hasnain javed", 34));
	}
}