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

import static org.slf4j.LoggerFactory.getLogger;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Crate specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
public class SimpleCratePersistentProperty extends AnnotationBasedPersistentProperty<CratePersistentProperty> implements CratePersistentProperty {
	
	private final Logger logger = getLogger(getClass()); 
	
	private static final String ID_FIELD_NAME = "_id";

	private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<String>();

	static {
		SUPPORTED_ID_PROPERTY_NAMES.add("id");
		SUPPORTED_ID_PROPERTY_NAMES.add("_id");
	}

	public SimpleCratePersistentProperty(Field field, PropertyDescriptor propertyDescriptor, 
										 PersistentEntity<?, CratePersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
		super(field, propertyDescriptor, owner, simpleTypeHolder);
		
		if (isIdProperty() && getFieldName() != ID_FIELD_NAME) {
			logger.warn("Customizing field name for id property not allowed! Custom name will not be considered!");
		}
	}

	@Override
	public String getFieldName() {
		
		if (isIdProperty()) {

			if (owner == null) {
				return ID_FIELD_NAME;
			}

			if (owner.getIdProperty() == null) {
				return ID_FIELD_NAME;
			}

			if (owner.isIdProperty(this)) {
				return ID_FIELD_NAME;
			}
		}
		
		return field.getName();
	}

	@Override
	public boolean isIdProperty() {
		return super.isIdProperty() || (field != null && SUPPORTED_ID_PROPERTY_NAMES.contains(getName()));
	}

	@Override
	protected Association<CratePersistentProperty> createAssociation() {
		throw new UnsupportedOperationException("@Reference is not supported!");
	}
}
