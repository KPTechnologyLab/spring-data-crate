/*
 * Copyright 2013 the original author or authors.
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

import org.springframework.data.crate.core.CrateOperations;

/**
 * Crate specific repository implementation. Likely to be used as target within
 * {@link org.springframework.data.crate.repository.support.CrateRepositoryFactory}
 *
 * @author Rizwan Idrees
 */
public class SimpleCrateRepository<T>  {

	public SimpleCrateRepository() {
		super();
	}

//	public SimpleCrateRepository(CrateEntityInformation<T, String> metadata,
//                                 CrateOperations crateOperations) {
//		super(metadata, crateOperations);
//	}

	public SimpleCrateRepository(CrateOperations crateOperations) {
		//todo:
	}

	//@Override
	protected String stringIdRepresentation(String id) {
		return id;
	}
}
