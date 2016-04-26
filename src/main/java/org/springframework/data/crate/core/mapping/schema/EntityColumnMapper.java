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
package org.springframework.data.crate.core.mapping.schema;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.CyclicReferenceBarrier.cyclicReferenceBarrier;
import static org.springframework.data.crate.core.mapping.CrateSimpleTypes.HOLDER;
import static org.springframework.data.util.ClassTypeInformation.from;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.data.crate.InvalidCrateApiUsageException;
import org.springframework.data.crate.core.CyclicReferenceBarrier;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.util.TypeInformation;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 * 
 * Creates columns from entity properties and maps them from java type to crate data type.
 * Nested arrays/collections of arrays/collections are not currently supported by crate db
 */
class EntityColumnMapper {
	
	private final static String PK_CONSTRAINT = "Composite primary key '%s' must contain primitive type field(s) only";
	
	private final Logger logger = getLogger(getClass());
	
	private final PrimitiveColumnMapper primitiveTypeMapper;
	private final PrimitiveCollectionTypeColumnMapper primitiveCollectionTypeMapper;
	private final MapTypeColumnMapper mapTypeMapper;
	private final MapCollectionTypeColumnMapper mapCollectionTypeMapper;
	private final MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;
	
	public EntityColumnMapper(MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		this.mappingContext = mappingContext;
		this.primitiveTypeMapper = new PrimitiveColumnMapper();
		this.primitiveCollectionTypeMapper = new PrimitiveCollectionTypeColumnMapper();
		this.mapTypeMapper = new MapTypeColumnMapper();
		this.mapCollectionTypeMapper = new MapCollectionTypeColumnMapper();
	}
	
	/**
	 * Creates columns from {@link CratePersistentEntity}
	 * @param entity entity object, must not be {@literal null}. 
	 * @return list of columns
	 */
	List<Column> toColumns(CratePersistentEntity<?> entity) {
		
		List<Column> columns = new LinkedList<>();
		
		mapColumns(entity, columns, cyclicReferenceBarrier());
		
		return columns;
	}
	
	/**
	 * Recursively crawls over a nested object's fields
	 * @param root entity object, must not be {@literal null}.
	 * @param columns list of columns for root entity object, must not be {@literal null}.
	 * @param barrier to detect potential cycles within entities.
	 * @param mappingContext must not be {@literal null}.
	 * @return list of columns (with optional list of subcloumns) of crate types
	 */
	private void mapColumns(CratePersistentEntity<?> root, List<Column> columns,  CyclicReferenceBarrier barrier) {
		
		notNull(root);
		notNull(columns);

		logger.debug("creating object column for type {}", root.getType());
		
		columns.addAll(primitiveTypeMapper.mapColumns(root.getPrimitiveProperties()));			
		columns.addAll(primitiveCollectionTypeMapper.mapColumns(filterPrimitiveCollectionType(root)));
		columns.addAll(mapTypeMapper.mapColumns(root.getMapProperties()));
		columns.addAll(mapCollectionTypeMapper.mapColumns(filterMapCollectionType(root)));
		
		Set<CratePersistentProperty> properties = root.getEntityProperties();
		properties.addAll(filterEntityCollectionType(root));
			
		for(CratePersistentProperty property : properties) {
			
			List<Column> subColumns = new LinkedList<>();
			
			CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(property);
			
			barrier.guard(property);
			
			mapColumns(entity, subColumns, barrier);
			
			Column column = createColumn(property);
			column.setSubColumns(subColumns);
			
			columns.add(column);
		}
	}
	
	private Column createColumn(CratePersistentProperty property) {
		
		notNull(property);
		
		Column column;
		
		if(property.isCollectionLike()) {
			column = new Column(property.getFieldName(), property.getRawType(), property.getComponentType());
		}else {
			column = new Column(property.getFieldName(), property.getRawType());
		}
		
		if(property.isIdProperty()) {
			validatePrimaryKey(property);
			column.setPrimaryKey(TRUE);
		}
		
		logger.debug("mapped field '{}' of type '{}' to crate type '{}'", new Object[]{property.getFieldName(),
																					   property.getRawType(),
																					   column.getCrateType()});
		return column;
	}
	
	private boolean isSimpleType(Class<?> type) {
		return HOLDER.isSimpleType(type);
	}
	
	private boolean isMapType(Class<?> type) {
		return from(type).isMap();
	}
	
	private Set<CratePersistentProperty> filterPrimitiveCollectionType(CratePersistentEntity<?> entity) {
		
		Set<CratePersistentProperty> filtered = new LinkedHashSet<>();
		
		List<CratePersistentProperty> properties = new LinkedList<>();
		properties.addAll(entity.getArrayProperties());
		properties.addAll(entity.getCollectionProperties());
		
		for(CratePersistentProperty property : properties) {
			
			checkNestedCollection(property);
			
			if(isSimpleType(property.getComponentType())) {
				filtered.add(property);
			}
		}
		
		return filtered;
	}
	
	private Set<CratePersistentProperty> filterEntityCollectionType(CratePersistentEntity<?> entity) {
		
		Set<CratePersistentProperty> filtered = new LinkedHashSet<>();
		
		List<CratePersistentProperty> properties = new LinkedList<>();
		properties.addAll(entity.getArrayProperties());
		properties.addAll(entity.getCollectionProperties());
		
		for(CratePersistentProperty property : properties) {
			
			checkNestedCollection(property);
			
			Class<?> componentType = property.getComponentType(); 
			
			if(!isSimpleType(componentType) && !isMapType(componentType)) {
				filtered.add(property);
			}
		}
		
		return filtered;
	}
	
	private Set<CratePersistentProperty> filterMapCollectionType(CratePersistentEntity<?> entity) {
		
		Set<CratePersistentProperty> filtered = new LinkedHashSet<>();
		
		List<CratePersistentProperty> properties = new LinkedList<>();
		properties.addAll(entity.getArrayProperties());
		properties.addAll(entity.getCollectionProperties());
		
		for(CratePersistentProperty property : properties) {
			
			checkNestedCollection(property);
			
			if(isMapType(property.getComponentType())) {
				filtered.add(property);
			}
		}
		
		return filtered;
	}
	
	private void checkNestedCollection(CratePersistentProperty property) {
		
		TypeInformation<?> componentType = from(property.getComponentType());
		
		if(componentType.isCollectionLike()) {
			throw new InvalidCrateApiUsageException("currently crate does not support nested arrays/collections of arrays/collections");
		}
	}
	
	private void checkMapKey(Class<?> componentType) {
		if(!isSimpleType(componentType)) {
			throw new InvalidCrateApiUsageException("Complex objects cannot be used as map's key");
		}
	}
	
	private void validatePrimaryKey(CratePersistentProperty property) {
		
		if(property.isArray() || 
		   property.isCollectionLike() || 
		   property.isMap()) {
			throw new InvalidCrateApiUsageException(format(PK_CONSTRAINT, property.getFieldName()));
		}
		
		if(property.isEntity()) {
			
			CratePersistentEntity<?> primaryKey = mappingContext.getPersistentEntity(property);
			
			if(!primaryKey.getArrayProperties().isEmpty() || 
			   !primaryKey.getCollectionProperties().isEmpty() || 
			   !primaryKey.getEntityProperties().isEmpty() || 
			   !primaryKey.getMapProperties().isEmpty()) {
				throw new InvalidCrateApiUsageException(format(PK_CONSTRAINT, property.getFieldName()));
			}
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * 
	 * Creates columns of primitive types
	 */
	private class PrimitiveColumnMapper {

		/**
		 * @param properties properties of primitive types, must not be {@literal null}.
		 * @return list of columns of crate primitives type 
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			notNull(properties);
			
			List<Column> columns = new ArrayList<>(properties.size());
			
			for(CratePersistentProperty property : properties) {
				Column column = createColumn(property);
				columns.add(column);
			}
			
			return columns;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * Creates array columns of array/collection of primitive types.
	 */
	private class PrimitiveCollectionTypeColumnMapper {
		
		/**
		 * @param properties properties of array/collection types, must not be {@literal null}.
		 * @return list of columns of crate type array
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			List<Column> columns = new LinkedList<>();
			
			for(CratePersistentProperty property : properties) {
				// safety check
				if(property.isCollectionLike() && isSimpleType(property.getComponentType())) {
					columns.add(createColumn(property));
				}
			}
			
			return columns;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * Creates array columns of array/collection of map types.
	 */
	private class MapCollectionTypeColumnMapper {
		
		/**
		 * @param properties properties of array/collection types, must not be {@literal null}.
		 * @return list of columns of crate type array
		 * @throws {@link InvalidCrateApiUsageException}
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			List<Column> columns = new LinkedList<>();
			
			for(CratePersistentProperty property : properties) {
				
				TypeInformation<?> typeInformation = from(property.getComponentType());
				// safety check
				if(property.isCollectionLike() && typeInformation.isMap()) {
					
					// could be a list or an array
					TypeInformation<?> actualType = property.getTypeInformation().getActualType();					
					// get the map's key type
					Class<?> componentType = actualType.getTypeArguments().get(0).getType();					
					
					checkMapKey(componentType);
					
					columns.add(createColumn(property));
				}
			}
			
			return columns;
		}
	}
	
	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * 
	 * Creates columns for map type properties.
	 * Map type properties will be mapped to crate type of object.
	 * The crate object type defining this object's behaviour is DYNAMIC and will have no schema.
	 * A schema will be created on the fly on first insert.
	 */
	private class MapTypeColumnMapper {
		
		/**
		 * @param properties map type properties, must not be {@literal null}.
		 * @return list of columns of crate type object
		 * @throws {@link InvalidCrateApiUsageException} for complex key types 
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			notNull(properties);
			
			List<Column> columns = new LinkedList<>();
			
			for(CratePersistentProperty property : properties) {
				// safety check
				if(property.isMap()) {
					checkMapKey(property.getComponentType());
					columns.add(createColumn(property));
				}
			}
			
			return columns;
		}
	}
}