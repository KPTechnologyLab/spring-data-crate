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
package org.springframework.data.crate;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * 
 * @author Hasnain Javed
 *  
 * @since 1.0.0
 */
public class InvalidCrateApiUsageException extends InvalidDataAccessApiUsageException {

	private static final long serialVersionUID = 3223184356539114174L;

	public InvalidCrateApiUsageException(String msg) {
		super(msg);
	}
	
	public InvalidCrateApiUsageException(String msg, Throwable cause) {
		super(msg, cause);
	}
}