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
package org.springframework.data.crate.core.mapping.event;

import org.springframework.data.crate.core.mapping.CrateDocument;

/**
 * Event being thrown after a single or a set of documents has/have been deleted. The {@link CrateDocument} held in the event
 * will be the query document <em>after</am> it has been mapped onto the domain type handled.
 * 
 * @author Martin Baumgartner
 * @author Hasnain Javed
 * @since 1.0.0 
 */
public class AfterDeleteEvent<T> extends CrateMappingEvent<T> {
	
	private static final long serialVersionUID = 1691362279201298878L;

	/**
	 * Creates a new {@link AfterDeleteEvent} for the given type.
	 * 
	 */
	public AfterDeleteEvent(T source) {
		super(source, null);
	}
}