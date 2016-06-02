package org.springframework.data.crate.core.sql;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class InsertTest {

    @Test
    public void shouldCreateInsertStatement() {

        String tableName = "entity";
        Set<String> columns = new HashSet<String>(asList("A", "B"));

        CrateSQLStatement statement = new Insert(tableName, columns);

        assertThat(statement.createStatement(), is("INSERT INTO entity (\"A\",\"B\") VALUES (?,?)"));
    }
}
