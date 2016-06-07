package org.springframework.data.crate.query;

import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.repository.query.RepositoryQuery;

public abstract class CrateRepositoryQuery implements RepositoryQuery {

    private final CrateOperations crateOperations;
    private final String query;

    protected CrateRepositoryQuery(String query, CrateOperations crateOperations) {
        this.query = query;
        this.crateOperations = crateOperations;
    }

    @Override
    public Object execute(Object[] parameters) {
        return null;
    }

}
