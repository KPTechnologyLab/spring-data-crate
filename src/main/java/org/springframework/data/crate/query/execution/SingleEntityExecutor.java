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

package org.springframework.data.crate.query.execution;

import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.query.CrateRepositoryQuery;
import org.springframework.data.crate.query.SimpleQueryCrateAction;
import org.springframework.data.crate.query.SimpleQueryCrateHandler;
import org.springframework.data.repository.query.QueryMethod;

import java.util.List;
import java.util.Locale;

import static org.springframework.util.Assert.isTrue;

public class SingleEntityExecutor extends QueryExecution {

    public SingleEntityExecutor(CrateOperations operations) {
        super(operations);
    }

    @Override
    protected Object doExecute(CrateRepositoryQuery query, Object[] values) {
        QueryMethod queryMethod = query.getQueryMethod();

        return getSingleResult(operations.execute(
                new SimpleQueryCrateAction(query.getSource()),
                new SimpleQueryCrateHandler<>(queryMethod.getReturnedObjectType()))
        );
    }

    private Object getSingleResult(List<?> results) {
        isTrue(results.size() <= 1, String.format(Locale.ENGLISH,
                "Select statement should return only one entity %d", results.size()));
        return results.size() == 1 ? results.get(0) : null;
    }
}
