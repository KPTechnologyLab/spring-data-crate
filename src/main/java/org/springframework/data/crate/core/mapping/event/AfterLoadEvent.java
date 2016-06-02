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

import static org.springframework.util.Assert.notNull;

/**
 * Event to be triggered after loading {@link CrateDocument}s to be mapped onto a given type.
 *
 * @author Oliver Gierke
 * @author Jon Brisbin
 * @author Christoph Leiter
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class AfterLoadEvent<T> extends CrateMappingEvent<CrateDocument> {

    private static final long serialVersionUID = 8004512960298852177L;

    private final Class<T> type;

    /**
     * Creates a new {@link AfterLoadEvent} for the given {@link CrateDocument} and type.
     *
     * @param document must not be {@literal null}.
     * @param type     must not be {@literal null}.
     */
    public AfterLoadEvent(CrateDocument document, Class<T> type) {

        super(document, document);
        notNull(type, "Type must not be null!");
        this.type = type;
    }

    /**
     * Returns the type for which the {@link AfterLoadEvent} shall be invoked for.
     *
     * @return
     */
    public Class<T> getType() {
        return type;
    }
}
