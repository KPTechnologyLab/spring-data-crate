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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.data.crate.core.mapping.CrateDocument;

import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

/**
 * Base class to implement domain class specific {@link ApplicationListener}s.
 *
 * @author Jon Brisbin
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author Hasnain Javed
 * @since 1.0.0
 */
public abstract class AbstractCrateEventListener<E> implements ApplicationListener<CrateMappingEvent<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCrateEventListener.class);

    private final Class<?> domainClass;

    /**
     * Creates a new {@link AbstractCrateEventListener}.
     */
    public AbstractCrateEventListener() {
        Class<?> typeArgument = resolveTypeArgument(this.getClass(), AbstractCrateEventListener.class);
        this.domainClass = typeArgument == null ? Object.class : typeArgument;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @SuppressWarnings({"unchecked"})
    public void onApplicationEvent(CrateMappingEvent<?> event) {

        if (event instanceof AfterLoadEvent) {

            AfterLoadEvent<?> afterLoadEvent = (AfterLoadEvent<?>) event;

            if (domainClass.isAssignableFrom(afterLoadEvent.getType())) {
                onAfterLoad(event.getDocument());
            }

            return;
        }

        E source = (E) event.getSource();

        // Check for matching domain type and invoke callbacks
        if (source != null && !domainClass.isAssignableFrom(source.getClass())) {
            return;
        }

        if (event instanceof BeforeDeleteEvent) {
            onBeforeDelete(source);
            return;
        } else if (event instanceof AfterDeleteEvent) {
            onAfterDelete(source);
            return;
        }

        if (event instanceof BeforeConvertEvent) {
            onBeforeConvert(source);
        } else if (event instanceof BeforeSaveEvent) {
            onBeforeSave(source, event.getDocument());
        } else if (event instanceof AfterSaveEvent) {
            onAfterSave(source, event.getDocument());
        } else if (event instanceof AfterConvertEvent) {
            onAfterConvert(event.getDocument(), source);
        }
    }

    public void onBeforeConvert(E source) {
        LOG.debug("onBeforeConvert({})", source);
    }

    public void onAfterConvert(CrateDocument document, E source) {
        LOG.debug("onAfterConvert({}, {})", document, source);
    }

    public void onBeforeSave(E source, CrateDocument document) {
        LOG.debug("onBeforeSave({}, {})", source, document);
    }

    public void onAfterSave(E source, CrateDocument document) {
        LOG.debug("onAfterSave({}, {})", source, document);
    }

    public void onBeforeDelete(E id) {
        LOG.debug("onBeforeDelete({})", id);
    }

    public void onAfterDelete(E id) {
        LOG.debug("onAfterDelete({})", id);
    }

    public void onAfterLoad(CrateDocument document) {
        LOG.debug("onAfterLoad({})", document);
    }
}
