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

import static java.lang.String.format;
import static org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy.INSTANCE;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Crate specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
public class SimpleCratePersistentProperty extends AnnotationBasedPersistentProperty<CratePersistentProperty> implements CratePersistentProperty {
	
	private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<String>(1);
	
	private final FieldNamingStrategy fieldNamingStrategy;
	
	private final static String RESERVED_ID = "'_id' is reserved in crate db and cannot be used as user-defined column name for '%s' in class '%s'";
	private final static String RESERVED_VERSION = "'_version' is reserved in crate db and cannot be used as user-defined column name for '%s' in class '%s'";
	private final static String STARTS_WITH_UNDERSCORE = "Column identity '%s' must not start with '_' in class '%s'";
	
	static {
		SUPPORTED_ID_PROPERTY_NAMES.add("id");
	}

	public SimpleCratePersistentProperty(Field field, PropertyDescriptor propertyDescriptor, 
										 PersistentEntity<?, CratePersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
		super(field, propertyDescriptor, owner, simpleTypeHolder);
		
		this.fieldNamingStrategy = INSTANCE;

		String fieldName = getFieldName();
		
		if(RESERVED_ID_FIELD_NAME.equals(fieldName)) {				
			throw new MappingException(format(RESERVED_ID, fieldName, owner.getType()));
		}
		
		if(RESERVED_VERSION_FIELD_NAME.equals(fieldName)) {
			throw new MappingException(format(RESERVED_VERSION, fieldName, owner.getType()));
		}
		
		if(startsWithIgnoreCase(fieldName, "_")) {
			throw new MappingException(format(STARTS_WITH_UNDERSCORE, fieldName, owner.getType()));
		}
	}
	
	/**
	 * Returns the key to be used to store the value of the property inside a Crate {@link CrateDBObject}.
	 * 
	 * @return name of field
	 */
	@Override
	public String getFieldName() {
		
		String fieldName = fieldNamingStrategy.getFieldName(this);
		
		if (!hasText(fieldName)) {
			throw new MappingException(format("Invalid (null or empty) field name returned for property %s by %s!",
											  this, fieldNamingStrategy.getClass()));
		}
		
		return fieldName;
	}

	@Override
	public boolean isIdProperty() {
		return super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getName());
	}
	
	@Override
	protected Association<CratePersistentProperty> createAssociation() {
		throw new UnsupportedOperationException("@Reference is not supported!");
	}
}