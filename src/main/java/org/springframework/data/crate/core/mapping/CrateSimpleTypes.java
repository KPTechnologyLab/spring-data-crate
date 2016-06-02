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

package org.springframework.data.crate.core.mapping;

import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CrateSimpleTypes {

    static {
        Set<Class<?>> simpleTypes = new HashSet<>();
        simpleTypes.add(CrateDocument.class);
        simpleTypes.add(CrateArray.class);
        simpleTypes.add(Boolean[].class);
        simpleTypes.add(Long[].class);
        simpleTypes.add(Short[].class);
        simpleTypes.add(Integer[].class);
        simpleTypes.add(Byte[].class);
        simpleTypes.add(Float[].class);
        simpleTypes.add(Double[].class);
        simpleTypes.add(Character[].class);
        CRATE_SIMPLE_TYPES = unmodifiableSet(simpleTypes);
    }

    private static final Set<Class<?>> CRATE_SIMPLE_TYPES;
    public static final SimpleTypeHolder HOLDER = new SimpleTypeHolder(CRATE_SIMPLE_TYPES, true);

    private CrateSimpleTypes() {
    }
}
