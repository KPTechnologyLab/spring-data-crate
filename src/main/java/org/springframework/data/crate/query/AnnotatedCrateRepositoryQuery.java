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
import org.springframework.data.crate.annotations.Query;
import org.springframework.data.crate.core.CrateOperations;

public class AnnotatedCrateRepositoryQuery extends CrateRepositoryQuery {

    public AnnotatedCrateRepositoryQuery(CrateQueryMethod queryMethod, CrateOperations crateOperations) {
        super(queryMethod, crateOperations);
        Preconditions.checkArgument(queryMethod.isAnnotated(),
                "Cannot create annotated query if an annotation doesn't contain a query.");
    }

    @Override
    protected String createQuery(Object[] parameters) {
        if (queryMethod.getAnnotatedQuery().isPresent()) {
            return queryMethod.getAnnotatedQuery().get();
        }
        throw new IllegalArgumentException("Cannot create annotated query if an annotation doesn't contain a query.");
    }
}
