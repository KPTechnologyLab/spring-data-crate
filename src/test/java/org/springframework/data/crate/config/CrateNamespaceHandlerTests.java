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

package org.springframework.data.crate.config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.data.crate.config.BeanNames.SCHEMA_EXPORT_MANAGER;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.crate.client.CrateClientFactoryBean;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;
import org.springframework.data.sample.repositories.SampleCrateRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Rizwan Idrees
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:namespace.xml")
public class CrateNamespaceHandlerTests {

    @Autowired
    private ApplicationContext context;

    @Test
    public void shouldCreateClient() {
        assertThat(context.getBean(CrateClientFactoryBean.class), is(notNullValue()));
        assertThat(context.getBean(CrateClientFactoryBean.class), is(instanceOf(CrateClientFactoryBean.class)));
    }

    @Test
    public void shouldCreateRepository() {
        assertThat(context.getBean(CrateClientFactoryBean.class), is(notNullValue()));
        assertThat(context.getBean(SampleCrateRepository.class),
                is(instanceOf(SampleCrateRepository.class)));
    }
    
    @Test
    public void shouldCreateSchemaExportManager() {
    	assertThat(context.getBean(SCHEMA_EXPORT_MANAGER, CratePersistentEntitySchemaManager.class), is(notNullValue()));
    }
}