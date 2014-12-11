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
package org.springframework.data.crate.config;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.data.crate.core.mapping.annotations.Table;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AbstractCrateConfigurationTest {

	@Test
	public void usesConfigClassPackageAsBaseMappingPackage() throws ClassNotFoundException {

		AbstractCrateConfiguration configuration = new SampleCrateConfiguration();
		assertThat(configuration.getMappingBasePackage(), is(SampleCrateConfiguration.class.getPackage().getName()));
		assertThat(configuration.getInitialEntitySet(), hasSize(1));
		assertThat(configuration.getInitialEntitySet(), hasItem(Entity.class));
	}
	
	class SampleCrateConfiguration extends AbstractCrateConfiguration {		
	}
	
	@Table(name="entity")
	static class Entity {
	}
}