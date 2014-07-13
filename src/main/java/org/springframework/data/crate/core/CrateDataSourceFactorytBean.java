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

package org.springframework.data.crate.core;

import static org.springframework.util.Assert.notNull;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CrateDataSourceFactorytBean implements FactoryBean<DataSource>, InitializingBean {
	
	private DataSource dataSource;
	
	public CrateDataSourceFactorytBean(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public DataSource getObject() throws Exception {
		return dataSource;
	}

	public Class<?> getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		notNull(dataSource, "A data source implementation is required");
	}
}