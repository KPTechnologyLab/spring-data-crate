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
package org.springframework.data.crate.core.convert;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.core.CollectionFactory.createCollection;
import static org.springframework.core.CollectionFactory.createMap;
import static org.springframework.data.crate.core.convert.CrateDocumentPropertyAccessor.INSTANCE;
import static org.springframework.data.crate.core.mapping.CratePersistentProperty.RESERVED_VESRION_FIELD_NAME;
import static org.springframework.data.util.ClassTypeInformation.MAP;
import static org.springframework.data.util.ClassTypeInformation.OBJECT;
import static org.springframework.data.util.ClassTypeInformation.from;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.CollectionUtils.arrayToList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.crate.core.mapping.CrateArray;
import org.springframework.data.crate.core.mapping.CrateDocument;
import org.springframework.data.crate.core.mapping.CratePersistentEntity;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor.Parameter;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.DefaultSpELExpressionEvaluator;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.mapping.model.SpELContext;
import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.mapping.model.SpELExpressionParameterValueProvider;
import org.springframework.data.util.TypeInformation;

/**
 * {@link CrateConverter} that uses a {@link MappingContext} for complex mapping
 * of domain objects to {@link CrateDocument}.
 * 
 * @author Rizwan Idrees
 * @author Hasnain Javed
 * @since 1.0.0
 */

public class MappingCrateConverter extends AbstractCrateConverter implements ApplicationContextAware {
	
	private final Logger logger = getLogger(getClass());
	
	protected final MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext;
	
	private final SpELContext spELContext;

	protected ApplicationContext applicationContext;

	protected CrateTypeMapper typeMapper;

	public MappingCrateConverter(MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> mappingContext) {
		super(new DefaultConversionService());
		notNull(mappingContext, "Mapping context is required.");
		this.mappingContext = mappingContext;
		this.spELContext = new SpELContext(INSTANCE);
		this.typeMapper = new DefaultCrateTypeMapper();
	}

	@Override
	public MappingContext<? extends CratePersistentEntity<?>, CratePersistentProperty> getMappingContext() {
		return mappingContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public <R> R read(Class<R> type, CrateDocument source) {
		return read(from(type), source, null);
	}

	@Override
	public void write(Object source, CrateDocument sink) {

		if(source == null) {
			return;
		}

		TypeInformation<?> type = from(source.getClass());

		if(!conversions.hasCustomWriteTarget(source.getClass(), sink.getClass())) {
			typeMapper.writeType(type, sink);
		}

		writeInternal(source, sink, type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object convertToCrateType(Object obj, TypeInformation<?> typeInformation) {
		
		if(obj == null) {
			return null;
		}
		
		if(conversions.isSimpleType(obj.getClass())) {
			return getPotentiallyConvertedSimpleWrite(obj);
		}
		
		Class<?> target = conversions.getCustomWriteTarget(obj.getClass());
		if(target != null) {
			return conversionService.convert(obj, target);
		}
		
		TypeInformation<?> typeHint = typeInformation == null ? OBJECT : typeInformation;
		
		if(obj instanceof CrateArray) {
			return maybeConvertList((CrateArray) obj, typeHint);
		}
		
		if(obj instanceof CrateDocument) {
			
			CrateDocument document = new CrateDocument();
			for (String key : ((CrateDocument) obj).keySet()) {
				Object o = ((CrateDocument) obj).get(key);
				document.put(key, convertToCrateType(o, typeHint));
			}
			return document;
		}
		
		if(obj instanceof Map) {
			
			CrateDocument document = new CrateDocument();
			for(Map.Entry<Object, Object> entry : ((Map<Object, Object>) obj).entrySet()) {
				document.put(entry.getKey().toString(), convertToCrateType(entry.getValue(), typeHint));
			}
			return document;
		}
		
		if(obj.getClass().isArray()) {
			return maybeConvertList(asList((Object[]) obj), typeHint);
		}

		if(obj instanceof Collection) {
			return maybeConvertList((Collection<?>) obj, typeHint);
		}
		
		CrateDocument document = new CrateDocument();
		this.write(obj, document);
		
		if(typeInformation == null) {
			return removeTypeInfoRecursively(document);
		}
		
		return !obj.getClass().equals(typeInformation.getType()) ? document : removeTypeInfoRecursively(document);
	}

	/**
	 * Read an incoming {@link CrateDocument} into the target entity.
	 *
	 * @param type the type information of the target entity.
	 * @param source the document to convert.
	 * @param parent an optional parent object.
	 * @param <R> the entity type.
	 * @return the converted entity.
	 */
	@SuppressWarnings("unchecked")
	protected <R> R read(final TypeInformation<R> type, final CrateDocument source, final Object parent) {
		
	    if(source == null) {
	    	return null;
	    }

	    TypeInformation<? extends R> typeToUse = typeMapper.readType(source, type);
	    Class<? extends R> rawType = typeToUse.getType();

	    if(conversions.hasCustomReadTarget(source.getClass(), rawType)) {
	      return conversionService.convert(source, rawType);
	    }

	    if(typeToUse.isMap()) {
	      return (R) readMap(typeToUse, source, parent);
	    }

	    CratePersistentEntity<R> entity = (CratePersistentEntity<R>) mappingContext.getPersistentEntity(typeToUse);
	    
	    if(entity == null) {
	      throw new MappingException("No mapping metadata found for " + rawType.getName());
	    }
	    
	    return read(entity, source, parent);
	}
	
	/**
	 * Read an incoming {@link CrateDocument} into the target entity.
	 *
	 * @param entity the target entity.
	 * @param source the document to convert.
	 * @param parent an optional parent object.
	 * @param <R> the entity type.
	 * @return the converted entity.
	 */
	@SuppressWarnings("unchecked")
	protected <R> R read(final CratePersistentEntity<R> entity, final CrateDocument source, final Object parent) {
		
		final DefaultSpELExpressionEvaluator evaluator = new DefaultSpELExpressionEvaluator(source, spELContext);
		
	    ParameterValueProvider<CratePersistentProperty> provider = getParameterProvider(entity, source, evaluator, parent);
	    
	    EntityInstantiator instantiator = instantiators.getInstantiatorFor(entity);

	    R instance = instantiator.createInstance(entity, provider);
	    final PersistentPropertyAccessor propertyAccessor = entity.getPropertyAccessor(instance);
	    final R result = (R)propertyAccessor.getBean();
	    final CratePersistentProperty idProperty = entity.getIdProperty();
	    final CratePersistentProperty versionProperty = entity.getVersionProperty();
	    
	    if(entity.hasIdProperty()) {
	    	Object idValue = getValueInternal(idProperty, source, result);
	    	propertyAccessor.setProperty(idProperty, idValue);
	    }
	    
	    if(entity.hasVersionProperty()) {
	    	Object versionValue = getValueInternal(versionProperty, source, result);
	    	propertyAccessor.setProperty(versionProperty, versionValue);
	    }
	    
	    for(CratePersistentProperty property : entity.getPersistentProperties()) {
	    	// skip id and version properties as they may have potentially been set above.  
			if((idProperty != null && idProperty.equals(property)) || (versionProperty != null && versionProperty.equals(property))) {
				continue;
			}
			
			if(!source.containsKey(property.getFieldName()) || entity.isConstructorArgument(property)) {
				continue;
			}
			
			propertyAccessor.setProperty(property, getValueInternal(property, source, result));
	    }
	    
	    entity.doWithAssociations(new AssociationHandler<CratePersistentProperty>() {
	    	
	      @Override
	      public void doWithAssociation(final Association<CratePersistentProperty> association) {	    	  
	    	  CratePersistentProperty inverseProp = association.getInverse();
	    	  Object obj = getValueInternal(inverseProp, source, result);
	    	  propertyAccessor.setProperty(inverseProp, obj);
	      }	      
	    });

	    return result;
	  }
	
	/**
	 * Recursively parses the a map from the source document.
	 *
	 * @param type the type information for the document.
	 * @param source the source document.
	 * @param parent the optional parent.
	 * @return the recursively parsed map.
	 */
	protected Map<Object, Object> readMap(final TypeInformation<?> type, final CrateDocument source, final Object parent) {
		
		notNull(source);
		
	    Class<?> mapType = typeMapper.readType(source, type).getType();
	    Map<Object, Object> map = createMap(mapType, source.keySet().size());
	    
	    for(Map.Entry<String, Object> entry : source.entrySet()) {
	    	
	    	Object key = entry.getKey();
	    	Object value = entry.getValue();

	        TypeInformation<?> keyTypeInformation = type.getComponentType();
	        
		    if(keyTypeInformation != null) {
		    	Class<?> keyType = keyTypeInformation.getType();
		        key = conversionService.convert(key, keyType);
		    }
	
		    TypeInformation<?> valueType = type.getMapValueType();
		    
		    if(value instanceof CrateDocument) {
		    	map.put(key, read(valueType, (CrateDocument) value, parent));
		    }else if(value instanceof CrateArray) {
		    	map.put(key, readCollection(valueType, (CrateArray) value, parent));
		    }else {
		    	Class<?> valueClass = valueType == null ? null : valueType.getType();
		        map.put(key, getPotentiallyConvertedSimpleRead(value, valueClass));
		    }
	    }
	    
	    return map;
	}
	
	/**
	 * Convert a source object into a {@link CrateDocument} target.
	 * 
	 * @param source the source object.
	 * @param sink the target document.
	 * @param typeHint the type information for the source.
	 */
	@SuppressWarnings("unchecked")
	protected void writeInternal(final Object source, CrateDocument sink, final TypeInformation<?> typeHint) {
		
		if(source == null) {
			return;
		}

		if(Collection.class.isAssignableFrom(source.getClass())) {
			throw new IllegalArgumentException("Root Document must be either CrateDocument or Map.");
		}
		
		Class<?> customTarget = getCustomWriteHandler(source.getClass(), CrateDocument.class);

		if(customTarget != null) {
			CrateDocument result = conversionService.convert(source, CrateDocument.class);
			sink.putAll(result);
			return;
		}

		if(Map.class.isAssignableFrom(source.getClass())) {
			writeMapInternal((Map<Object, Object>) source, sink, MAP);
			return;
		}

		CratePersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());
		
		writeInternal(source, sink, entity);
		
		addCustomTypeKeyIfNecessary(typeHint, source, sink);
	}
	
	/**
	   * Internal helper method to write the source object into the target document.
	   *
	   * @param source the source object.
	   * @param sink the target document.
	   * @param entity the persistent entity to convert from.
	   */
	  protected void writeInternal(final Object source, final CrateDocument sink, final CratePersistentEntity<?> entity) {
		  
	    if(source == null) {
	      return;
	    }

	    if(entity == null) {
	      throw new MappingException("No mapping metadata found for entity ".concat(source.getClass().getName()));
	    }
	    
		final PersistentPropertyAccessor propertyAccessor = entity.getPropertyAccessor(source);
	    final CratePersistentProperty idProperty = entity.getIdProperty();
	    final CratePersistentProperty versionProperty = entity.getVersionProperty();
	    
	    if(idProperty != null && !sink.containsKey(idProperty.getFieldName())) {
	    	try {
	    		Object id = convertToCrateType(propertyAccessor.getProperty(idProperty), idProperty.getTypeInformation());
	    		sink.put(idProperty.getFieldName(), id);
	    	}catch(ConversionException e) {
	    		logger.warn("Failed to convert id property '{}'. {}", new Object[]{idProperty.getFieldName(),
	    								 										   e.getMessage()});
	    	}
	    }
	    
	    for(CratePersistentProperty property : entity.getPersistentProperties()) {
	    	
	    	if(property.equals(idProperty) || (versionProperty != null && property.equals(versionProperty))) {
		          continue;
	    	}
	    	
	    	Object propertyObj = propertyAccessor.getProperty(property/*, property.getType()*/);
	    	
	        if(propertyObj != null) {
	        	if(!conversions.isSimpleType(propertyObj.getClass()) || isPrimitiveArray(property)) {
	        		writePropertyInternal(propertyObj, sink, property);
	        	}else {
	        		writeSimpleInternal(propertyObj, sink, property.getFieldName());
	        	}
	        }
	    }
	    
	    entity.doWithAssociations(new AssociationHandler<CratePersistentProperty>() {
	    	@Override
	    	public void doWithAssociation(final Association<CratePersistentProperty> association) {
	    		CratePersistentProperty inverse = association.getInverse();
	    		Object propertyObj = propertyAccessor.getProperty(inverse);
	    		if (propertyObj != null) {
	    			writePropertyInternal(propertyObj, sink, inverse);
	    		}
	    	}
	    });
	  }

	/**
	   * Helper method to write a property into the target crate document.
	   *
	   * @param source the source object.
	   * @param sink the target document.
	   * @param property the property information.
	   */
	  @SuppressWarnings("unchecked")
	  private void writePropertyInternal(final Object source, final CrateDocument sink, final CratePersistentProperty property) {
		  
		  if(source == null) {
			  return;
		  }
		  
		  String name = property.getFieldName();
		  TypeInformation<?> valueType = from(source.getClass());
		  TypeInformation<?> type = property.getTypeInformation();
	
		  if(valueType.isCollectionLike()) {
			  CrateArray array = writeCollection(asCollection(source), property);
		      sink.put(name, array);
		      return;
		  }
		  
		  if(valueType.isMap()) {
			  CrateDocument document = writeMap((Map<Object, Object>) source, property);
			  sink.put(name, document);
		      return;
		  }
		  
		  Class<?> basicTargetType = conversions.getCustomWriteTarget(source.getClass(), null);
		  
		  if(basicTargetType != null) {
			  sink.put(name, conversionService.convert(source, basicTargetType));
		      return;
		  }
		  
		  CrateDocument document = new CrateDocument();
		  addCustomTypeKeyIfNecessary(type, source, document);
	
		  CratePersistentEntity<?> entity = isSubtype(property.getType(), source.getClass()) ? mappingContext .getPersistentEntity(source.getClass()) : 
		    																				   mappingContext.getPersistentEntity(type);
		  writeInternal(source, document, entity);
		    
		  sink.put(name, document);
	  }

	/**
	 * Helper method to write the map into the crate document.
	 * 
	 * @param source the source object.
	 * @param sink the target document.
	 * @param type the type information for the document.
	 * @return the written crate document.
	 */
	private CrateDocument writeMapInternal(final Map<Object, Object> source, final CrateDocument sink, final TypeInformation<?> type) {

		for(Map.Entry<Object, Object> entry : source.entrySet()) {
			
			Object key = entry.getKey();
			Object val = entry.getValue();
			
			if(conversions.isSimpleType(key.getClass())) {
				
				String simpleKey = key.toString();
				
				if(val == null || (conversions.isSimpleType(val.getClass()) && !val.getClass().isArray())) {
					writeSimpleInternal(val, sink, simpleKey);
				}else if(val instanceof Collection || val.getClass().isArray()) {
					sink.put(simpleKey, writeCollectionInternal(asCollection(val), new CrateArray(), type.getMapValueType()));
				}else {
					CrateDocument document = new CrateDocument();
					TypeInformation<?> valueTypeInfo = type.isMap() ? type.getMapValueType() : OBJECT;
					writeInternal(val, document, valueTypeInfo);
					sink.put(simpleKey, document);
				}
			} else {
				throw new MappingException("Cannot use a complex object as a key value.");
			}
		}
		
		return sink;
	}
	
	/**
	   * Helper method to write the internal collection.
	   *
	   * @param source the source object.
	   * @param target the target document.
	   * @param type the type information for the document.
	   * @return the created crate list.
	   */
	private CrateArray writeCollectionInternal(final Collection<?> source, final CrateArray target, final TypeInformation<?> type) {
		
		TypeInformation<?> componentType = type == null ? null : type.getComponentType();

	    for(Object element : source) {
	    	
	    	validateCollectionLikeElement(element);
	    	
	    	Class<?> elementType = element == null ? null : element.getClass();
	    	
	    	if(elementType == null || conversions.isSimpleType(elementType)) {
	    		target.add(element);
	    	}else {
	    		CrateDocument document = new CrateDocument();
	    		writeInternal(element, document, componentType);
	    		target.add(document);
	    	}
	    }
	    
	    return target;
	}
	
	/**
	 * Read a collection from the source object.
	 *
	 * @param targetType the target type.
	 * @param source the list as source.
	 * @param parent the optional parent.
	 * @return the converted {@link Collection} or array, will never be {@literal null}.
	 */
	private Object readCollection(final TypeInformation<?> targetType, final CrateArray source, final Object parent) {
		
		notNull(targetType);

	    Class<?> collectionType = targetType.getType();
	    
	    if(source.isEmpty()) {
	      return getPotentiallyConvertedSimpleRead(new HashSet<Object>(), collectionType);
	    }

	    collectionType = Collection.class.isAssignableFrom(collectionType) ? collectionType : List.class;
	    
	    Collection<Object> items = targetType.getType().isArray() ? new ArrayList<Object>(source.size()) :
	    															createCollection(collectionType, source.size());
	    
	    TypeInformation<?> componentType = targetType.getComponentType();
	    
	    Class<?> rawComponentType = componentType == null ? null : componentType.getType();

	    for(Object object : source) {
	    	if(object instanceof CrateDocument) {
	    		items.add(read(componentType, (CrateDocument) object, parent));
	    	}else {
	    		items.add(getPotentiallyConvertedSimpleRead(object, rawComponentType));
	    	}
	    }
	    
	    return getPotentiallyConvertedSimpleRead(items, targetType.getType());
	  }
	
	/**
	 * Writes the given simple value to the given {@link CrateDocument}. Will store enum names for enum values.
	 * 
	 * @param value
	 * @param CrateDocument must not be {@literal null}.
	 * @param key must not be {@literal null}.
	 */
	private void writeSimpleInternal(final Object source, final CrateDocument sink, final String key) {
		sink.put(key, getPotentiallyConvertedSimpleWrite(source));
	}
	
	/**
	 * Returns given object as {@link Collection}. Will return the
	 * {@link Collection} as is if the source is a {@link Collection} already,
	 * will convert an array into a {@link Collection} or simply create a single
	 * element collection for everything else.
	 * 
	 * @param source
	 * @return
	 */
	private static Collection<?> asCollection(final Object source) {
		if(source instanceof Collection) {
			return (Collection<?>) source;
		}
		return source.getClass().isArray() ? arrayToList(source) : singleton(source);
	}
	
	/**
	 * Adds custom type information to the given {@link CrateDocument} if necessary.
	 *  
	 * @param type
	 * @param value must not be {@literal null}.
	 * @param document must not be {@literal null}.
	 */
	protected void addCustomTypeKeyIfNecessary(TypeInformation<?> type, Object value, CrateDocument document) {

		TypeInformation<?> actualType = type != null ? type.getActualType() : null;
		Class<?> reference = actualType == null ? Object.class : actualType.getType();
		Class<?> valueType = getUserClass(value.getClass());

		boolean notTheSameClass = !valueType.equals(reference);
		if(notTheSameClass) {
			typeMapper.writeType(valueType, document);
		}
	}
	
	/**
	 * Checks whether we have a custom conversion registered for the given value into an arbitrary simple Crate type.
	 * Returns the converted value if so. If not, we perform special enum handling or simply return the value as is.
	 * 
	 * @param value
	 * @return
	 */
	private Object getPotentiallyConvertedSimpleWrite(Object value) {
		
		if(value == null) {
			return null;
		}
		
		Class<?> customTarget = conversions.getCustomWriteTarget(value.getClass(), null);

		if(customTarget != null) {
			return conversionService.convert(value, customTarget);
		} else {
			return Enum.class.isAssignableFrom(value.getClass()) ? ((Enum<?>) value).toString() : value;
		}
	}
	
	/**
	   * Helper method to read the value based on the value type.
	   *
	   * @param value the value to convert.
	   * @param type the type information.
	   * @param parent the optional parent.
	   * @param <R> the target type.
	   * @return the converted object.
	   */
	  @SuppressWarnings("unchecked")
	  private <R> R readValue(Object value, TypeInformation<?> type, Object parent) {
		  
		  Class<?> rawType = type.getType();
		  
		  if(conversions.hasCustomReadTarget(value.getClass(), rawType)) {
			  return (R) conversionService.convert(value, rawType);			  
		  }else if(value instanceof CrateDocument) {
			  return (R) read(type, (CrateDocument) value, parent);
		  }else if(value instanceof CrateArray) {
			  return (R) readCollection(type, (CrateArray) value, parent);
		  } else {
			  return (R) getPotentiallyConvertedSimpleRead(value, rawType);
		  }
	  }
	
	/**
	 * Checks whether we have a custom conversion for the given simple object. Converts the given value if so, applies
	 * {@link Enum} handling or returns the value as is.
	 * 
	 * @param value
	 * @param target must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getPotentiallyConvertedSimpleRead(Object value, Class<?> target) {

		if(value == null || target == null) {
			return value;
		}

		if(conversions.hasCustomReadTarget(value.getClass(), target)) {
			return conversionService.convert(value, target);
		}

		if(Enum.class.isAssignableFrom(target)) {
			return Enum.valueOf((Class<Enum>) target, value.toString());
		}

		return target.isAssignableFrom(value.getClass()) ? value : conversionService.convert(value, target);
	}
	
	private CrateArray maybeConvertList(Iterable<?> source, TypeInformation<?> typeInformation) {
		
		CrateArray array = new CrateArray();
		
		for(Object element : source) {
			validateCollectionLikeElement(element);
			array.add(convertToCrateType(element, typeInformation));
		}
		
		return array;
	}
	
	/**
	 * Loads the property value through the value provider.
	 *
	 * @param property the source property.
	 * @param source the source document.
	 * @param parent the optional parent.
	 * @return the actual property value.
	 */
	protected Object getValueInternal(final CratePersistentProperty property, final CrateDocument source, final Object parent) {
		  return new CratePropertyValueProvider(source, spELContext, parent).getPropertyValue(property);
	  }
	
	/**
	   * Helper method to create the underlying collection/list.
	   *
	   * @param collection the collection to write.
	   * @param property the property information.
	   * @return the created crate array.
	   */
	private CrateArray writeCollection(final Collection<?> collection, final CratePersistentProperty property) {
		return writeCollectionInternal(collection, new CrateArray(), property.getTypeInformation());
	}
	
	/**
	   * Wrapper method to create the underlying map.
	   *
	   * @param map the source map.
	   * @param property the persistent property.
	   * @return the written crate document.
	   */
	private CrateDocument writeMap(final Map<Object, Object> map, final CratePersistentProperty property) {
		notNull(map, "Source map must not be null");
	    notNull(property, "PersistentProperty must not be null");
	    return writeMapInternal(map, new CrateDocument(), property.getTypeInformation());
	}
	
	/**
	   * Check if one class is a subtype of the other.
	   *
	   * @param left the first class.
	   * @param right the second class.
	   * @return true if it is a subtype, false otherwise.
	   */
	private boolean isSubtype(final Class<?> left, final Class<?> right) {
		return left.isAssignableFrom(right) && !left.equals(right);
	}
	
	private boolean isPrimitiveArray(CratePersistentProperty property) {
		return property.isArray() && conversions.isSimpleType(property.getType());
	}
	
	/**
	 * Removes the type information from the conversion result.
	 * 
	 * @param object
	 * @return
	 */
	private Object removeTypeInfoRecursively(Object object) {
		
		if(!(object instanceof CrateDocument)) {
			return object;
		}

		CrateDocument document = (CrateDocument) object;
		
		String keyToRemove = null;
		
		for(String key : document.keySet()) {
			
			if (typeMapper.isTypeKey(key)) {
				keyToRemove = key;
			}

			Object value = document.get(key);
			
			if (value instanceof CrateArray) {
				for (Object element : (CrateArray) value) {
					removeTypeInfoRecursively(element);
				}
			} else {
				removeTypeInfoRecursively(value);
			}
		}
		
		if (keyToRemove != null) {
			document.remove(keyToRemove);
		}
		
		return document;
	}
	
	/**
	   * Creates a new parameter provider.
	   *
	   * @param entity the persistent entity.
	   * @param source the source document.
	   * @param evaluator the SPEL expression evaluator.
	   * @param parent the optional parent.
	   * @return a new parameter value provider.
	   */
	private ParameterValueProvider<CratePersistentProperty> getParameterProvider(final CratePersistentEntity<?> entity, final CrateDocument source, 
																				 final DefaultSpELExpressionEvaluator evaluator, final Object parent) {
		
	    CratePropertyValueProvider provider = new CratePropertyValueProvider(source, evaluator, parent);
	    
	    PersistentEntityParameterValueProvider<CratePersistentProperty> parameterProvider = new PersistentEntityParameterValueProvider<CratePersistentProperty>(entity, provider, parent);

	    return new ConverterAwareSpELExpressionParameterValueProvider(evaluator, conversionService, parameterProvider, parent);
	}
	
	private Class<?> getCustomWriteHandler(Class<?> sourceClass, Class<?> sinkClass) {		
		return conversions.getCustomWriteTarget(sourceClass, sinkClass);
	}
	
	private void validateCollectionLikeElement(Object element) {
		if(element != null && (element instanceof Collection || element.getClass().isArray())) {
			throw new MappingException("Nesting Array or Collection types is not supported by crate");
		}
	}
	
	/**
	 * Property value provider for Crate documents.
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class CratePropertyValueProvider implements PropertyValueProvider<CratePersistentProperty> {

	    /**
	     * The source document.
	     */
	    private final CrateDocument source;

	    /**
	     * The expression evaluator.
	     */
	    private final SpELExpressionEvaluator evaluator;

	    /**
	     * The optional parent object.
	     */
	    private final Object parent;

	    public CratePropertyValueProvider(final CrateDocument source, final SpELContext factory, final Object parent) {
	      this(source, new DefaultSpELExpressionEvaluator(source, factory), parent);
	    }

	    public CratePropertyValueProvider(final CrateDocument source, final DefaultSpELExpressionEvaluator evaluator, final Object parent) {
	      notNull(source);
	      notNull(evaluator);

	      this.source = source;
	      this.evaluator = evaluator;
	      this.parent = parent;
	    }

	    @Override
	    public <R> R getPropertyValue(final CratePersistentProperty property) {
	    	
	      String expression = property.getSpelExpression();
	      
	      Object value;
	    		  
	      if(expression != null) {
	    	  value = evaluator.evaluate(expression);
	      }else {
	    	  value = property.isVersionProperty() ? source.get(RESERVED_VESRION_FIELD_NAME) : source.get(property.getFieldName());
	      }
	      
	      if(value == null) {
	        return null;
	      }
	      
	      return readValue(value, property.getTypeInformation(), parent);
	    }
	}
	
	/**
	 * An expression parameter value provider.
	 * @author Hasnain Javed
	 * @since 1.0.0
	 */
	private class ConverterAwareSpELExpressionParameterValueProvider extends SpELExpressionParameterValueProvider<CratePersistentProperty> {
		
		private final Object parent;
		
	    public ConverterAwareSpELExpressionParameterValueProvider(final SpELExpressionEvaluator evaluator, final ConversionService conversionService, 
	    														  final ParameterValueProvider<CratePersistentProperty> delegate, final Object parent) {
	      super(evaluator, conversionService, delegate);
	      this.parent = parent;
	    }
	    
	    @Override
	    protected <T> T potentiallyConvertSpelValue(final Object object, final Parameter<T, CratePersistentProperty> parameter) {
	    	return readValue(object, parameter.getType(), parent);
	    }
	}
}