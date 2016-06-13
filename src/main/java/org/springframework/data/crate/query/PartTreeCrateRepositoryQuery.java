/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package org.springframework.data.crate.query;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.mapping.CratePersistentProperty;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

public class PartTreeCrateRepositoryQuery extends CrateRepositoryQuery {

    private final MappingContext<?, CratePersistentProperty> mappingContext;
    private final Set<String> roots;
    private final PartTree tree;

    public PartTreeCrateRepositoryQuery(CrateQueryMethod queryMethod, CrateOperations crateOperations) {
        super(queryMethod, crateOperations);
        this.mappingContext = crateOperations.getConverter().getMappingContext();

        Class<?> domainClass = queryMethod.getEntityInformation().getJavaType();
        this.tree = new PartTree(queryMethod.getName(), domainClass);
        this.roots = getRoots(domainClass.getAnnotation(Table.class));
    }

    public String createQuery(Object[] parameters) {
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        CrateQueryCreator creator = new CrateQueryCreator(tree, roots, accessor, mappingContext);
        MethodQuery query = creator.createQuery();
        return query.buildSQLString().toString();
    }

    private Set<String> getRoots(Table table) {
        Preconditions.checkNotNull(table, "An entity must be annotated with @Table.");
        String root = table.name();
        if (hasText(root)) {
            return ImmutableSet.of(root);
        }
        throw new IllegalArgumentException("Query source is not provided.");
    }
}
