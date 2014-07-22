package org.springframework.data.crate.core;

import io.crate.action.sql.SQLResponse;
import io.crate.client.CrateClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.sample.entities.Book;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author Rizwan Idrees
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:crate-template-test.xml")
public class CrateTemplateTests {

    @Autowired
    private CrateTemplate crateTemplate;
    @Autowired
    private CrateClient client;

    @Before
    public void before(){
        // TODO: DROP TABLE IF EXISTS
        // client.sql("DROP TABLE Book").actionGet();
    }

    @Test
    @Ignore
    public void shouldCreateTableForGivenClass(){
        //When
        crateTemplate.createTable(Book.class);
        //Then
        SQLResponse response = client.sql("SELECT table_name FROM information_schema.tables where table_name = 'Book'").actionGet();
        assertThat(response.rowCount(), is(greaterThan(0L)));
        assertThat((String) response.rows()[0][0] , is("Book"));
    }


}