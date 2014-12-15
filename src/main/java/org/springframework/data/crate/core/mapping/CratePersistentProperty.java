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
package org.springframework.data.crate.core.mapping;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.PersistentProperty;

/**
 * CratePersistentProperty
 *
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */

public interface CratePersistentProperty extends PersistentProperty<CratePersistentProperty> {
	
	String RESERVED_ID_FIELD_NAME = "_id";
	String RESERVED_VESRION_FIELD_NAME = "_version";
	long INITIAL_VERSION_VALUE = 1;
	
	String getFieldName();

	public enum PropertyToFieldNameConverter implements Converter<CratePersistentProperty, String> {

		INSTANCE;

		public String convert(CratePersistentProperty source) {
			return source.getFieldName();
		}
	}
}