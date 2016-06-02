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

import java.util.ArrayList;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class SimpleMappingEventListener extends AbstractCrateEventListener<Object> {

    public final ArrayList<BeforeConvertEvent<Object>> onBeforeConvertEvents = new ArrayList<BeforeConvertEvent<Object>>();
    public final ArrayList<AfterConvertEvent<Object>> onAfterConvertEvents = new ArrayList<AfterConvertEvent<Object>>();
    public final ArrayList<BeforeSaveEvent<Object>> onBeforeSaveEvents = new ArrayList<BeforeSaveEvent<Object>>();
    public final ArrayList<AfterSaveEvent<Object>> onAfterSaveEvents = new ArrayList<AfterSaveEvent<Object>>();
    public final ArrayList<BeforeDeleteEvent<Object>> onBeforeDeleteEvents = new ArrayList<BeforeDeleteEvent<Object>>();
    public final ArrayList<AfterDeleteEvent<Object>> onAfterDeleteEvents = new ArrayList<AfterDeleteEvent<Object>>();
    public final ArrayList<AfterLoadEvent<Object>> onAfterLoadEvents = new ArrayList<AfterLoadEvent<Object>>();


    @Override
    public void onBeforeConvert(Object source) {
        onBeforeConvertEvents.add(new BeforeConvertEvent<Object>(source));
    }

    @Override
    public void onAfterConvert(CrateDocument document, Object source) {
        onAfterConvertEvents.add(new AfterConvertEvent<Object>(document, source));
    }

    @Override
    public void onBeforeSave(Object source, CrateDocument document) {
        onBeforeSaveEvents.add(new BeforeSaveEvent<Object>(source, document));
    }

    @Override
    public void onAfterSave(Object source, CrateDocument document) {
        onAfterSaveEvents.add(new AfterSaveEvent<Object>(source, document));
    }

    @Override
    public void onBeforeDelete(Object id) {
        onBeforeDeleteEvents.add(new BeforeDeleteEvent<Object>(id));
    }

    @Override
    public void onAfterDelete(Object id) {
        onAfterDeleteEvents.add(new AfterDeleteEvent<Object>(id));
    }

    @Override
    public void onAfterLoad(CrateDocument document) {
        onAfterLoadEvents.add(new AfterLoadEvent<Object>(document, Object.class));
    }

    public void clearEvents() {
        onBeforeConvertEvents.clear();
        onAfterConvertEvents.clear();
        onBeforeSaveEvents.clear();
        onAfterSaveEvents.clear();
        onBeforeDeleteEvents.clear();
        onAfterConvertEvents.clear();
        onAfterLoadEvents.clear();
    }
}
