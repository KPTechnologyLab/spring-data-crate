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
 * Creates table definitions for persistent entities.
 * Creates columns from entity properties and maps them from java type to crate data type.
 * Nested arrays/collections of arrays/collections are not currently supported by crate db
 */
class EntityTableMapper {
	
	private final Logger logger = getLogger(getClass());
	
	private final PrimitiveColumnMapper primitiveTypeMapper;
	private final PrimitiveCollectionTypeColumnMapper primitiveCollectionTypeMapper;
	private final MapTypeColumnMapper mapTypeMapper;
	
	private final MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;
	
	private EntityTableMapper(MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		this.mappingContext = mappingContext;
		this.primitiveTypeMapper = new PrimitiveColumnMapper();
		this.primitiveCollectionTypeMapper = new PrimitiveCollectionTypeColumnMapper();
		this.mapTypeMapper = new MapTypeColumnMapper();
	}
	
	public static EntityTableMapper entityTableMapper(MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		return new EntityTableMapper(mappingContext);
	}
	
	/**
	 * @param entity entity object, must not be {@literal null}. 
	 * @return table definition containing table name and columns
	 */
	public TableDefinition createDefinition(CratePersistentEntity<?> entity) {
		
		List<Column> columns = createColumns(entity);
		
		return new TableDefinition(entity.getTableName(), columns);
	}
	
	/**
	 * @param entity entity object, must not be {@literal null}. 
	 * @return list of columns
	 */
	public List<Column> createColumns(CratePersistentEntity<?> entity) {
		
		List<Column> columns = new LinkedList<Column>();
		
		mapColumns(entity, columns, cyclicReferenceBarrier());
		
		return columns;
	}
	
	/**
	 * Recursively crawls over a nested object's fields
	 * @param root entity object, must not be {@literal null}.
	 * @param columns list of columns fot root entity object, must not be {@literal null}.
	 * @param barrier to detect potential cycles within entities.
	 * @param mappingContext must not be {@literal null}.
	 * @return list of columns (with optional list of subcloumns) of crate type object
	 */
	private void mapColumns(CratePersistentEntity<?> root, List<Column> columns,  CyclicReferenceBarrier barrier) {
		
		notNull(root);
		notNull(columns);
		
		logger.debug("creating object column for type {}", root.getType());
		
		columns.addAll(primitiveTypeMapper.mapColumns(root.getPrimitiveProperties()));			
		columns.addAll(primitiveCollectionTypeMapper.mapColumns(filterPrimitiveCollectionType(root.getArrayProperties())));
		columns.addAll(primitiveCollectionTypeMapper.mapColumns(filterPrimitiveCollectionType(root.getCollectionProperties())));
		columns.addAll(mapTypeMapper.mapColumns(root.getMapProperties()));
		
		Set<CratePersistentProperty> properties = root.getEntityProperties();
		properties.addAll(filterEntityCollectionType(root.getArrayProperties()));
		properties.addAll(filterEntityCollectionType(root.getCollectionProperties()));
			
		for(CratePersistentProperty property : properties) {
			
			List<Column> subColumns = new LinkedList<Column>();
			
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
		
		Column column = new Column(property.getFieldName(), property.getRawType());
		
		if(property.isIdProperty()) {
			column.setPrimaryKey(TRUE);
		}
		
		if(property.isCollectionLike()) {
			column.setElementType(property.getComponentType());
		}
		
		logger.debug("mapped property type {} to crate type {}", property.getRawType(), column.getType());
		
		return column;
	}
	
	private boolean isPrimitiveElementType(CratePersistentProperty collectionTypeProperty) {
		return HOLDER.isSimpleType(collectionTypeProperty.getComponentType());
	}
	
	private Set<CratePersistentProperty> filterPrimitiveCollectionType(Set<CratePersistentProperty> properties) {
		
		Set<CratePersistentProperty> filtered = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			checkNestedCollection(property);
			
			if(isPrimitiveElementType(property)) {
				filtered.add(property);
			}
		}
		
		return filtered;
	}
	
	private Set<CratePersistentProperty> filterEntityCollectionType(Set<CratePersistentProperty> properties) {
		
		Set<CratePersistentProperty> filtered = new LinkedHashSet<CratePersistentProperty>();
		
		for(CratePersistentProperty property : properties) {
			
			checkNestedCollection(property);
			
			if(!isPrimitiveElementType(property)) {
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
			
			List<Column> columns = new ArrayList<Column>(properties.size());
			
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
			
			List<Column> columns = new LinkedList<Column>();
			
			for(CratePersistentProperty property : properties) {
				// safety check
				if(property.isCollectionLike() && isPrimitiveElementType(property)) {
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
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			notNull(properties);
			
			List<Column> columns = new LinkedList<Column>();
			
			for(CratePersistentProperty property : properties) {
				// safety check
				if(property.isMap()) {
					columns.add(createColumn(property));
				}
			}
			
			return columns;
		}
	}
}