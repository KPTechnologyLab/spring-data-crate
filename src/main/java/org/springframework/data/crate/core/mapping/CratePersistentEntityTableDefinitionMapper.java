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

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.crate.core.mapping.CrateDataType.ARRAY;
import static org.springframework.data.crate.core.mapping.CrateDataType.getCrateTypeFor;
import static org.springframework.data.util.ClassTypeInformation.from;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.data.crate.InvalidCrateApiUsageException;
import org.springframework.data.util.TypeInformation;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 * 
 * Creates table definitions for persistent entities.
 * The table definitions may be used to generate crate specific DDL
 */
public class CratePersistentEntityTableDefinitionMapper implements TableDefinitionMapper {
	
	private final Logger logger = getLogger(getClass());
	
	private final CrateMappingContext mappingContext;
	private final EntityColumnMapper entityTypeMapper;
	private final PrimitiveColumnMapper primitiveTypeMapper;
	private final CollectionTypeColumnMapper collectionTypeMapper;
	private final MapTypeColumnMapper mapTypeMapper;
	
	public CratePersistentEntityTableDefinitionMapper(CrateMappingContext mappingContext) {
		super();
		this.mappingContext = mappingContext;
		entityTypeMapper = new EntityColumnMapper();
		primitiveTypeMapper = new PrimitiveColumnMapper();
		collectionTypeMapper = new CollectionTypeColumnMapper();
		mapTypeMapper = new MapTypeColumnMapper();
	}
	
	@Override
	public TableDefinition createDefinition(CratePersistentEntity<?> entity) {
		
		logger.debug("generating table definition for {}", entity.getType());
		
		List<Column> columns = entityTypeMapper.mapColumns(entity);
		
		TableDefinition table = new TableDefinition();
		table.setName(entity.getTableName());
		table.setColumns(columns);
		
		return table;
	}
	
	private Column createColumn(CratePersistentProperty property) {
		
		notNull(property);
		
		Column column = new Column();
		column.setName(property.getFieldName());
		column.setType(getCrateTypeFor(property.getRawType()));
		
		if(property.isIdProperty()) {
			column.setPrimaryKey(TRUE);
		}
		
		if(property.isCollectionLike()) {
			column.setElementType(getCrateTypeFor(property.getComponentType()));
		}
		
		logger.debug("mapped property type {} to crate type {}", property.getRawType(), column.getType());
		
		return column;
	}
	
	private boolean isPrimitiveElementType(CratePersistentProperty collectionTypeProperty) {
		return mappingContext.isSimpleType(collectionTypeProperty.getComponentType());
	}

	public static class TableDefinition {
		
		private String name;
		
		private List<Column> columns;

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			hasText(name);
			this.name = name;
		}
		
		public List<Column> getColumns() {
			return columns;
		}
		
		public void setColumns(List<Column> columns) {
			notNull(columns);
			this.columns = columns;
		}
	}
	
	public static class Column {
		
		private String name;
		private String type;
		private String elementType;
		
		private boolean primaryKey;
		
		private List<Column> subColumns;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			hasText(name);
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			hasText(type);
			this.type = type;
		}
		
		public String getElementType() {
			return elementType;
		}

		public void setElementType(String elementType) {
			hasText(elementType);
			this.elementType = elementType;
		}

		public boolean isPrimaryKey() {
			return primaryKey;
		}

		public void setPrimaryKey(boolean primaryKey) {
			this.primaryKey = primaryKey;
		}

		public List<Column> getSubColumns() {
			
			if(subColumns == null) {
				return emptyList();
			}
			
			return subColumns;
		}

		public void setSubColumns(List<Column> subColumns) {
			this.subColumns = subColumns;
		}

		public boolean isArrayColumn() {
			return ARRAY.equalsIgnoreCase(type);
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
			
			List<Column> columns = new ArrayList<CratePersistentEntityTableDefinitionMapper.Column>(properties.size());
			
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
	 * Creates array columns of primitive types and entities.
	 */
	private class CollectionTypeColumnMapper {
		
		/**
		 * @param properties properties of primitive/entity types, must not be {@literal null}.
		 * @return list of columns of crate type array
		 */
		public List<Column> mapColumns(Set<CratePersistentProperty> properties) {
			
			List<Column> columns = new LinkedList<CratePersistentEntityTableDefinitionMapper.Column>();
			
			for(CratePersistentProperty property : properties) {
				
				// safety check
				if(property.isCollectionLike()) {
					
					if(isPrimitiveElementType(property)) {
						columns.add(createColumn(property));
					}else {
						CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(property.getComponentType());
						columns.addAll(entityTypeMapper.mapColumns(entity));
					}
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
			
			List<Column> columns = new LinkedList<CratePersistentEntityTableDefinitionMapper.Column>();
			
			for(CratePersistentProperty property : properties) {
				
				// safety check
				if(property.isMap()) {
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
	 * Creates columns for entity (nested object) type properties.
	 * Nested arrays/collections of arrays/collections are not currently supported by crate db
	 */
	private class EntityColumnMapper {
		
		/**
		 * @param entity entity object, must not be {@literal null}.
		 * @return list of columns of crate type object
		 */
		public List<Column> mapColumns(CratePersistentEntity<?> entity) {
			
			List<Column> columns = new LinkedList<CratePersistentEntityTableDefinitionMapper.Column>();
			
			mapColumns(entity, columns);
			
			return columns;
		}
		
		/**
		 * Recursively iterates over a nested object's fields
		 * @param root entity object, must not be {@literal null}.
		 * @param columns list of columns fot root entity object, must not be {@literal null}.
		 * @return list of columns (with optional list of subcloumns) of crate type object
		 */
		private void mapColumns(CratePersistentEntity<?> root, List<Column> columns) {
			
			notNull(root);
			notNull(columns);
			
			logger.debug("creating object column for type {}", root.getType());
			
			columns.addAll(primitiveTypeMapper.mapColumns(root.getPrimitiveProperties()));
			columns.addAll(mapTypeMapper.mapColumns(root.getMapProperties()));
			columns.addAll(collectionTypeMapper.mapColumns(filterPrimitiveCollectionType(root.getArrayProperties())));
			columns.addAll(collectionTypeMapper.mapColumns(filterPrimitiveCollectionType(root.getCollectionProperties())));
			
			Set<CratePersistentProperty> properties = root.getEntityProperties();
			properties.addAll(filterEntityCollectionType(root.getArrayProperties()));
			properties.addAll(filterEntityCollectionType(root.getCollectionProperties()));
			
			for(CratePersistentProperty property : properties) {
				
				List<Column> subColumns = new LinkedList<CratePersistentEntityTableDefinitionMapper.Column>();
				
				CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(property);
				
				mapColumns(entity, subColumns);
				
				Column column = createColumn(property);
				column.setSubColumns(subColumns);
				
				columns.add(column);
			}
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
				throw new InvalidCrateApiUsageException("currently crate does not support nested arrays/collections");
			}
		}
	}
}