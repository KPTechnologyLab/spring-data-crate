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
package org.springframework.data.crate.core.convert;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

/**
 * CrateConverter
 *
 * @author Rizwan Idrees
 */

public interface CrateConverter {

	/**
	 * Returns the underlying {@link org.springframework.data.mapping.context.MappingContext} used by the converter.
	 *
	 * @return never {@literal null}
	 */
	MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> getMappingContext();

	/**
	 * Returns the underlying {@link org.springframework.core.convert.ConversionService} used by the converter.
	 *
	 * @return never {@literal null}.
	 */
	ConversionService getConversionService();
}
