/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
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
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;
import org.springframework.data.crate.repository.config.EnableCrateRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.sample.entities.person.Person;
import org.springframework.data.sample.repositories.custom.CustomPersonCrateRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CreateLookupStrategyIntegrationTest.TestConfig.class})
public class CreateLookupStrategyIntegrationTest extends CrateIntegrationTest {

    @Autowired
    private CustomPersonCrateRepository repository;

    @Before
    public void setup() throws InterruptedException {
        ensureGreen();
        List<Person> persons = asList(
                new Person("person11@test.com", "person1", 25),
                new Person("person12@test.com", "person1", 27),
                new Person("person2@test.com", "person2", 40),
                new Person("person3@test.com", "person3", 34),
                new Person("person4@test.com", "person4", 21),
                new Person("person5@test.com", "person5", 50)
        );
        repository.save(persons);
        repository.refreshTable();
    }

    @After
    public void teardown() throws InterruptedException {
        repository.deleteAll();
    }

    @Test
    public void testSimpleCollectionQuery() {
        List<Person> persons = repository.findByName("person1");
        assertThat(persons.size(), is(2));
        assertThat(persons.get(0).getEmail(), not(persons.get(1).getEmail()));
    }

    @Test
    public void testSimpleNotCollectionQuery() {
        Person person = repository.getByEmail("person5@test.com");
        assertThat(person.getAge(), is(50));
    }

    @Test
    public void testCollectionQueryWithAnd() {
        Person persons = repository.getByNameAndEmail("person3", "person3@test.com");
        assertThat(persons.getAge(), is(34));
    }

    @Test
    public void testCollectionQueryWithOr() {
        List<Person> persons = repository.findByNameOrAge("person3", 25);
        assertThat(persons.size(), is(2));
    }

    @Test
    public void testCollectionQueryStartingWith() {
        List<Person> persons = repository.findByNameStartingWith("person1");
        assertThat(persons.size(), is(2));
    }

    @Test
    public void testCollectionQueryGreaterThanEqual() {
        List<Person> persons = repository.findByAgeGreaterThanEqual(34);
        assertThat(persons.size(), is(3));
    }

    @Configuration
    @EnableCrateRepositories(
            basePackages = "org.springframework.data.sample.repositories.custom",
            queryLookupStrategy = QueryLookupStrategy.Key.CREATE)
    static class TestConfig extends TestCrateConfiguration {

        @Bean
        public CrateClient crateClient() {
            return new CrateClient(String.format(Locale.ENGLISH, "%s:%d", server.crateHost(), server.transportPort()));
        }

        @Bean
        public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
            return new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP);
        }

        @Override
        protected String getMappingBasePackage() {
            return "org.springframework.data.sample.entities.person";
        }
    }
}
