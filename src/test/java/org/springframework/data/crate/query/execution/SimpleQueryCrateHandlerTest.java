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

package org.springframework.data.crate.query.execution;

import io.crate.action.sql.SQLResponse;
import io.crate.types.DataType;
import io.crate.types.IntegerType;
import io.crate.types.ObjectType;
import io.crate.types.StringType;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.crate.core.CrateActionResponseHandler;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SimpleQueryCrateHandlerTest {

    private static CrateActionResponseHandler handler;

    @BeforeClass
    public static void beforeClass() {
        handler = new SimpleQueryCrateHandler<>(User.class);
    }

    @Test
    public void testHandleMultipleRecords() {
        SQLResponse response = new SQLResponse();
        response.cols(new String[]{"name", "age", "email"});
        response.rows(new Object[][]{
                new Object[]{"name1", 1, new HashMap<String, Object>() {{
                    put("address", "1@gmail.com");
                }}},
                new Object[]{"name2", 2, new HashMap<String, Object>() {{
                    put("address", "2@gmail.com");
                }}}
        });
        response.colTypes(new DataType[]{StringType.INSTANCE, IntegerType.INSTANCE, ObjectType.INSTANCE});
        response.rowCount(2L);

        User user1 = new User("name1", new Email("1@gmail.com"), 1);
        User user2 = new User("name2", new Email("2@gmail.com"), 2);

        List<User> users = (List<User>) handler.handle(response);
        assertThat(users.size(), is(2));
        assertThat(users.get(0).hashCode(), is(user1.hashCode()));
        assertThat(users.get(1).hashCode(), is(user2.hashCode()));
    }

    private class Email {

        private final String address;

        public Email(String address) {
            this.address = address;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(15, 19)
                    .append(address)
                    .toHashCode();
        }
    }

    private class User {

        private final String name;
        private final Email email;
        private final int age;

        public User(String name, Email email, int age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(15, 19)
                    .append(name)
                    .append(age)
                    .append(email)
                    .toHashCode();
        }
    }
}
