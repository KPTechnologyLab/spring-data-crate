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

import static java.lang.String.format;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CyclicReferenceException extends RuntimeException {

    private static final long serialVersionUID = 6018750758104136572L;

    private final String propertyName;
    private final Class<?> type;
    private final String dotPath;

    public CyclicReferenceException(String propertyName, Class<?> type, String dotPath) {
        this.propertyName = propertyName;
        this.type = type;
        this.dotPath = dotPath;
    }

    @Override
    public String getMessage() {
        return format("Found cycle for field '%s' in type '%s' for path '%s'", propertyName, type.getName(), dotPath);
    }
}
