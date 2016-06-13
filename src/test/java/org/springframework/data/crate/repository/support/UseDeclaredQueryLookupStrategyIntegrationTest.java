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

import io.crate.client.CrateClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.crate.CrateIntegrationTest;
import org.springframework.data.crate.config.TestCrateConfiguration;
import org.springframework.data.crate.repository.config.EnableCrateRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.sample.entities.person.Person;
import org.springframework.data.sample.repositories.PersonRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UseDeclaredQueryLookupStrategyIntegrationTest.TestConfig.class})
public class UseDeclaredQueryLookupStrategyIntegrationTest extends CrateIntegrationTest {

    @Autowired
    PersonRepository repository;

    @Before
    public void setup() throws InterruptedException {
        List<Person> persons = asList(new Person("person1@test.com", "person1", 34),
                new Person("person2@test.com", "person2", 33));
        repository.save(persons);
        repository.refreshTable();
    }

    @After
    public void teardown() throws InterruptedException {
        repository.deleteAll();
    }

    @Test
    public void testSimpleAnnotation() {
        List<Person> persons = repository.getAll();
        assertThat(persons.size(), is(2));
        assertThat(persons.get(0).getName(), is("person1"));
    }

    @Test
    public void testParametrizedAnnotation() {
        List<Person> persons = repository.findByName("person2");
        assertThat(persons.size(), is(1));
        assertThat(persons.get(0).getEmail(), is("person2@test.com"));
    }

    @Configuration
    @EnableCrateRepositories(basePackages = "org.springframework.data.sample.repositories",
            queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
    static class TestConfig extends TestCrateConfiguration {

        @Bean
        public CrateClient crateClient() {
            return new CrateClient(String.format(Locale.ENGLISH, "%s:%d", server.crateHost(), server.transportPort()));
        }

        @Override
        protected String getMappingBasePackage() {
            return "org.springframework.data.sample.entities.person";
        }
    }
}
