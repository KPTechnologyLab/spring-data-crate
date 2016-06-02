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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.crate.core.mapping.schema.TableParameters;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.replace;


/**
 * Crate specific {@link org.springframework.data.mapping.PersistentEntity} implementation holding
 *
 * @param <T>
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
public class SimpleCratePersistentEntity<T> extends BasicPersistentEntity<T, CratePersistentProperty>
        implements CratePersistentEntity<T>, ApplicationContextAware {

    private final static String VERSION_TYPE = "Version property '%s' must be of type java.lang.Long";

    private final StandardEvaluationContext context;

    private final String tableName;
    private final TableParameters parameters;

    public SimpleCratePersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);
        this.context = new StandardEvaluationContext();
        this.tableName = resolveTableName(typeInformation);
        this.parameters = resolveTableParameters();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context.addPropertyAccessor(new BeanFactoryAccessor());
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        context.setRootObject(applicationContext);
    }

    @Override
    public void addPersistentProperty(CratePersistentProperty property) {

        super.addPersistentProperty(property);

        if (property.isVersionProperty() && !isLongType(property.getType())) {
            throw new MappingException(format(VERSION_TYPE, property.getFieldName()));
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public TableParameters getTableParameters() {
        return parameters;
    }

    @Override
    public Set<String> getPropertyNames(String... exclude) {

        Set<CratePersistentProperty> properties = getPersistentProperties();

        Set<String> excluded = new HashSet<>(asList(exclude));

        Set<String> propertyNames = new TreeSet<>();

        for (CratePersistentProperty property : properties) {
            if (!excluded.contains(property.getFieldName())) {
                propertyNames.add(property.getFieldName());
            }
        }

        return propertyNames;
    }

    /**
     * Returns all fields excluding static and transient fields
     */
    @Override
    public Set<CratePersistentProperty> getPersistentProperties() {

        final Set<CratePersistentProperty> properties = new LinkedHashSet<>();

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
        Set<CratePersistentProperty> simpleProperties = new LinkedHashSet<>();

        for (CratePersistentProperty property : properties) {

            boolean isCollectionLike = property.isCollectionLike();
            boolean isMap = property.isMap();
            boolean isEntity = property.isEntity();

            if (!isCollectionLike && !isMap && !isEntity) {
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
        Set<CratePersistentProperty> compositeProperties = new LinkedHashSet<>();

        for (CratePersistentProperty property : properties) {

            boolean isCollectionLike = property.isCollectionLike();
            boolean isMap = property.isMap();
            boolean isEntity = property.isEntity();

            if (!isCollectionLike && !isMap && isEntity) {
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
        Set<CratePersistentProperty> collectionProperties = new LinkedHashSet<>();

        for (CratePersistentProperty property : properties) {

            boolean isCollectionLike = property.isCollectionLike();
            boolean isArray = property.isArray();

            if (isCollectionLike && !isArray) {
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
        Set<CratePersistentProperty> mapProperties = new LinkedHashSet<>();

        for (CratePersistentProperty property : properties) {

            if (property.isMap()) {
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
        Set<CratePersistentProperty> arrayProperties = new LinkedHashSet<>();

        for (CratePersistentProperty property : properties) {

            if (property.isArray()) {
                arrayProperties.add(property);
            }
        }

        return arrayProperties;
    }

    private String resolveTableName(TypeInformation<T> typeInformation) {

        String fallback = replace(typeInformation.getType().getName(), ".", "_");

        String tableName;

        Table annotation = findAnnotation(Table.class);

        if (annotation != null) {
            tableName = hasText(annotation.name()) ? replace(annotation.name(), " ", "_") : fallback;
        } else {
            tableName = fallback;
        }

        return tableName;
    }

    private TableParameters resolveTableParameters() {

        TableParameters parameters = null;

        Table annotation = findAnnotation(Table.class);

        if (annotation != null) {
            parameters = new TableParameters(annotation.numberOfReplicas(),
                    annotation.refreshInterval(),
                    annotation.columnPolicy());
        }

        return parameters;
    }

    private boolean isLongType(Class<?> clazz) {
        return Long.class.equals(clazz) || Long.TYPE.equals(clazz);
    }
}
