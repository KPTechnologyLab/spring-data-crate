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

package org.springframework.data.crate.query;

import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.annotations.Query;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrateQueryCreatorTest {

    private final CrateMappingContext mappingContext = new CrateMappingContext();
    private final Set<String> roots = new HashSet<String>() {{
        add("users");
    }};

    @Test
    public void testCreateQueryWithOrCriteriaChain() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("findByAgeLessThanEqualOrNameIsLessThan", Integer.class, String.class);
        CrateQueryCreator creator = buildQueryCreator(method, new Object[]{18, "some name"});
        assertThat(creator.createQuery().buildSQLString().toString(),
                is("SELECT * FROM users WHERE age <= 18 OR name < 'some name'"));
    }

    @Test
    public void testCreateQueryWithAndOrCriteriaChain() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("getByIdAndAgeOrName",
                String.class, Integer.class, String.class);
        CrateQueryCreator creator = buildQueryCreator(method, new Object[]{"id", 18, "some name"});
        assertThat(creator.createQuery().buildSQLString().toString(),
                is("SELECT * FROM users WHERE (id = 'id' AND age = 18) OR name = 'some name'"));
    }

    @Test
    public void testCreateQueryWithAndAndLikeCriteriaChain() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("getByNameLikeAndLastNameStartingWith",
                String.class, String.class);
        CrateQueryCreator creator = buildQueryCreator(method, new Object[]{"first", "last"});
        assertThat(creator.createQuery().buildSQLString().toString(),
                is("SELECT * FROM users WHERE name LIKE 'first' AND lastName LIKE 'last%'"));
    }

    private CrateQueryCreator buildQueryCreator(Method method, Object[] values) {
        PartTree partTree = new PartTree(method.getName(), User.class);
        CrateQueryMethod queryMethod = new CrateQueryMethod(
                method,
                new DefaultRepositoryMetadata(UserRepository.class),
                new SpelAwareProxyProjectionFactory()
        );
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), values);
        return new CrateQueryCreator(partTree, roots, accessor, mappingContext);
    }

    interface UserRepository extends Repository<User, Long> {

        List<User> findByAgeLessThanEqualOrNameIsLessThan(Integer age, String name);

        List<User> getByIdAndAgeOrName(String id, Integer age, String name);

        List<User> getByNameLikeAndLastNameStartingWith(String name, String lastName);
    }

    @Table(name = "users")
    public class User {

        @Id
        private String id;
        private String name;
        private int age;
        private String lastName;

        public User(String id, String name, int age, String lastName) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.lastName = lastName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
