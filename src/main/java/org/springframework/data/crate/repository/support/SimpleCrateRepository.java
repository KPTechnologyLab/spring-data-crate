/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.crate.repository.support;

import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.repository.CrateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Crate specific repository implementation. Likely to be used as target within
 * {@link org.springframework.data.crate.repository.support.CrateRepositoryFactory}
 *
 * @author Rizwan Idrees
 */
public class SimpleCrateRepository<T, ID extends Serializable> implements CrateRepository<T,ID> {

    private CrateOperations crateOperations;
    private Class<T> entityClass;
    private CrateEntityInformation<T, ID> entityInformation;



    public SimpleCrateRepository(CrateOperations crateOperations,
                                 CrateEntityInformation<T, ID> entityInformation,
                                 Class<T> entityClass
    ) {
        Assert.notNull(crateOperations, "CrateOperations must be configured!");
        Assert.notNull(entityInformation, "entityInformation is missing");
        Assert.notNull(entityClass, "entityClass is missing");
        this.crateOperations = crateOperations;
        this.entityInformation = entityInformation;
    }

    @Override
    public Iterable<T> findAll(Sort orders) {
        //TODO:implement me
        return null;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        //TODO:implement me
        return null;
    }

    @Override
    public <S extends T> S save(S s) {
        //TODO:implement me
        return null;
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> ses) {
        //TODO:implement me
        return null;
    }

    @Override
    public T findOne(ID id) {
        //TODO:implement me
        return null;
    }

    @Override
    public boolean exists(ID id) {
        //TODO:implement me
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        //TODO:implement me
        return null;
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        //TODO:implement me
        return null;
    }

    @Override
    public long count() {
        //TODO:implement me
        return 0;
    }

    @Override
    public void delete(ID id) {
        //TODO:implement me
    }

    @Override
    public void delete(T t) {
        //TODO:implement me
    }

    @Override
    public void delete(Iterable<? extends T> ts) {
        //TODO:implement me
    }

    @Override
    public void deleteAll() {
        //TODO:implement me
    }
}
