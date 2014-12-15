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

package org.springframework.data.crate.client;

import static org.springframework.util.Assert.hasText;
import io.crate.client.CrateClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * CrateDataSourceFactoryBean
 *
 * @author Hasnain Javed
 * @author Rizwan Idrees
 */
public class CrateClientFactoryBean implements FactoryBean<CrateClient>, InitializingBean, DisposableBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    private CrateClient client;
	private String servers ="localhost:4300";

    @Override
	public CrateClient getObject() throws Exception {
		return client;
	}

    @Override
	public Class<CrateClient> getObjectType() {
		return CrateClient.class;
	}

    @Override
	public boolean isSingleton() {
		return true;
	}

    @Override
	public void afterPropertiesSet()  {
        hasText(servers, "[Assertion failed] servers settings missing.");
        client = new CrateClient(servers);
	}
    
    @Override
	public void destroy() throws Exception {
    	logger.info("closing crate client");
		client.close();
	}
    
    public void setServers(String servers) {
        this.servers = servers;
    }
}