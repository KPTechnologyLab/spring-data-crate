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


/**
 * Base class for delete events.
 *
 * @author Martin Baumgartner
 * @author Hasnain Javed
 * @since 1.0.0
 */
public abstract class AbstractDeleteEvent<T, E> extends CrateMappingEvent<T> {

    private static final long serialVersionUID = -5190716892116238395L;

    private final Class<E> type;

    /**
     * Creates a new {@link AbstractDeleteEvent} for the given source and type.
     *
     * @param source must not be {@literal null}.
     * @param type   the source's type. must not be {@literal null}.
     */
    public AbstractDeleteEvent(T source, Class<E> type) {
        super(source, null);
        this.type = type;
    }

    /**
     * Returns the type for which the {@link AbstractDeleteEvent} shall be invoked for.
     *
     * @return
     */
    public Class<E> getType() {
        return type;
    }
}
