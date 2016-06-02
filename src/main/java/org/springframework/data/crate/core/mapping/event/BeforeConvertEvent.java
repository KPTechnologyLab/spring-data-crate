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
 * Event being thrown before a domain object is converted to be persisted.
 *
 * @author Jon Brisbin
 * @author Oliver Gierke
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class BeforeConvertEvent<T> extends CrateMappingEvent<T> {

    private static final long serialVersionUID = 1658430047662705202L;

    public BeforeConvertEvent(T source) {
        super(source, null);
    }
}
