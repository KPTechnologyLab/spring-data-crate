package org.springframework.data.crate.query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.data.crate.annotations.Query;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.crate.core.mapping.event.User;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.sample.entities.SampleEntity;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CrateRepositoryQueryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetAnnotatedQuery() throws NoSuchMethodException {
        CrateQueryMethod repositoryMethod = prepareQueryMethod("selectFromNodes", SampleEntity.class);
        AnnotatedCrateRepositoryQuery repositoryQuery = new AnnotatedCrateRepositoryQuery(repositoryMethod, mock(CrateOperations.class));
        assertThat(repositoryQuery, is(instanceOf(CrateRepositoryQuery.class)));
        repositoryQuery.execute(new Object[]{});
        assertThat(repositoryQuery.getSource(), is("select * from sys.nodes"));
    }

    @Test
    public void testNoQueryAnnotation() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot create annotated query if an annotation doesn't contain a query.");
        CrateQueryMethod repositoryMethod = prepareQueryMethod("findAll", SampleEntity.class);
        new AnnotatedCrateRepositoryQuery(repositoryMethod, mock(CrateOperations.class));
    }

    interface AnnotatedCrateRepository {

        @Query("select * from sys.nodes")
        List<SampleEntity> selectFromNodes();

        List<SampleEntity> findAll();
    }

    private CrateQueryMethod prepareQueryMethod(String methodName, Class<?> entityClass) throws NoSuchMethodException {
        RepositoryMetadata repositoryMetadata = Mockito.mock(RepositoryMetadata.class);
        when(repositoryMetadata.getDomainType()).thenReturn((Class) entityClass);

        Method testMethod = AnnotatedCrateRepository.class.getMethod(methodName);
        when(repositoryMetadata.getReturnedDomainClass(testMethod)).thenReturn((Class) entityClass);

        return new CrateQueryMethod(testMethod, repositoryMetadata, mock(ProjectionFactory.class));
    }

}
