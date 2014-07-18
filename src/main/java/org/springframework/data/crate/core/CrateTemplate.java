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

import static org.slf4j.LoggerFactory.getLogger;

import javax.sql.DataSource;

import io.crate.client.CrateClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crate.core.convert.CrateConverter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Hasnain Javed
 * @author Rizwan Idrees
 *
 * @since 1.0.0
 */
public class CrateTemplate implements CrateOperations {

    private static final Logger logger = LoggerFactory.getLogger(CrateTemplate.class);
	private final CrateClient crateClient;
	
	public CrateTemplate(CrateClient crateClient) {
        this.crateClient = crateClient;
	}

    @Override
    public CrateConverter getConverter() {
        return null;
    }
}