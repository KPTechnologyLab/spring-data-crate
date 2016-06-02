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
package org.springframework.data.crate.core.convert;

import org.springframework.data.convert.EntityWriter;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.util.TypeInformation;

/**
 * CrateWriter marker interface for converting an object of type T to crate document representation {@link CrateDocument}.
 *
 * @param <T> the type of the object to be converted to a CrateDocument
 * @author Hasnain Javed
 * @since 1.0.0
 */
public interface CrateWriter<T> extends EntityWriter<T, CrateDocument> {

    /**
     * Converts the given object into a representation that Crate will be able to store natively but retains the type information in case
     * the given {@link TypeInformation} differs from the given object type.
     * One possible use case is to convert complex id types
     *
     * @param obj             can be {@literal null}.
     * @param typeInformation can be {@literal null}.
     * @return
     */
    Object convertToCrateType(Object obj, TypeInformation<?> typeInformation);
}
