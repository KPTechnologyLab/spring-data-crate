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

import io.crate.client.CrateClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.crate.CrateIntegrationTest;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.ConstraintViolationException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ValidatingCrateEventListenerTest.TestConfiguration.class})
public class ValidatingCrateEventListenerTest extends CrateIntegrationTest {

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


    @Configuration
    static class TestConfiguration extends LifecycleEventConfigurationBase {

        @Bean
        public CrateClient crateClient() {
            return new CrateClient(String.format(Locale.ENGLISH, "%s:%d", server.crateHost(), server.transportPort()));
        }

    }
}
