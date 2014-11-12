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

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.StringUtils.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Crate specific {@link org.springframework.data.mapping.PersistentEntity} implementation holding
 *
 * @param <T>
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
public class SimpleCratePersistentEntity<T> extends BasicPersistentEntity<T, CratePersistentProperty>
		implements CratePersistentEntity<T>, ApplicationContextAware {

	private final StandardEvaluationContext context;
	
	private final String tableName;

	public SimpleCratePersistentEntity(TypeInformation<T> typeInformation) {
		super(typeInformation);
		this.context = new StandardEvaluationContext();
		tableName = resolveTableName(typeInformation);
 	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context.addPropertyAccessor(new BeanFactoryAccessor());
		context.setBeanResolver(new BeanFactoryResolver(applicationContext));
		context.setRootObject(applicationContext);
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * Returns all fields excluding static and transient fields
	 */
	@Override
	public Set<CratePersistentProperty> getPersistentProperties() {

		final Set<CratePersistentProperty> properties = new LinkedHashSet<CratePersistentProperty>();
		
		doWithProperties(new PropertyHandler<CratePersistentProperty>() {
			@Override
			public void doWithPersistentProperty(CratePersistentProperty persistentProperty) {
				properties.add(persistentProperty);
			}
		});
		
		return properties;
	}
	
	/**
	 * Returns all primitive fields (String, int, etc)
	 */
	@Override
	public Set<CratePersistentProperty> getPrimitiveProperties() {
		
		Set<CratePersistentProperty> properties = getPersistentProperties();
		Set<CratePersistentProperty> simpleProperties = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			boolean isCollectionLike = property.isCollectionLike();
			boolean isMap = property.isMap();
			boolean isEntity = property.isEntity();
			
			if(!isCollectionLike && !isMap && !isEntity) {
				simpleProperties.add(property);
			}
		}
		
		return simpleProperties;
	}

	/**
	 * Returns all non primitive (entity) type fields
	 */
	@Override
	public Set<CratePersistentProperty> getEntityProperties() {
		
		Set<CratePersistentProperty> properties = getPersistentProperties();
		Set<CratePersistentProperty> compositeProperties = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			boolean isCollectionLike = property.isCollectionLike();
			boolean isMap = property.isMap();
			boolean isEntity = property.isEntity();
			
			if(!isCollectionLike && !isMap && isEntity) {
				compositeProperties.add(property);
			}
		}
		
		return compositeProperties;
	}
	
	/**
	 * Returns all java.util.Collection implementations
	 */
	@Override
	public Set<CratePersistentProperty> getCollectionProperties() {
		
		Set<CratePersistentProperty> properties = getPersistentProperties();
		Set<CratePersistentProperty> collectionProperties = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			boolean isCollectionLike = property.isCollectionLike();
			boolean isArray = property.isArray();
			
			if(isCollectionLike && !isArray) {
				collectionProperties.add(property);
			}
		}
		
		return collectionProperties;
	}
	
	/**
	 * Returns all java.util.Map implementations
	 */
	@Override
	public Set<CratePersistentProperty> getMapProperties() {
		
		Set<CratePersistentProperty> properties = getPersistentProperties();
		Set<CratePersistentProperty> mapProperties = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			if(property.isMap()) {
				mapProperties.add(property);
			}
		}
		
		return mapProperties;
	}
	
	/**
	 * Returns all array fields
	 */
	@Override
	public Set<CratePersistentProperty> getArrayProperties() {
		
		Set<CratePersistentProperty> properties = getPersistentProperties();
		Set<CratePersistentProperty> arrayProperties = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			if(property.isArray()) {
				arrayProperties.add(property);
			}
		}
		
		return arrayProperties;
	}
	
	private String resolveTableName(TypeInformation<T> typeInformation) {
		
		String tableName = null;
		
		Class<T> clazz = typeInformation.getType();
		
		if(clazz.isAnnotationPresent(Table.class)) {
			String name = typeInformation.getType().getAnnotation(Table.class).name();
			hasText(name, "Invalid name. Make sure the name is defined. e.g @Table(name=\"foo\")");
			tableName = name;
		}else {
			tableName = replace(clazz.getName(), ".", "_").toLowerCase();
		}
		
		return tableName;
 	}
}