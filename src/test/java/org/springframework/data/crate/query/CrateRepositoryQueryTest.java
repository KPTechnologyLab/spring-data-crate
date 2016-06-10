package org.springframework.data.crate.query;

import org.junit.Test;
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

    @Test
    public void testGetAnnotatedQuery() throws Exception {
        CrateQueryMethod repositoryMethod = prepareQueryMethod("selectFromNodes", SampleEntity.class);
        RepositoryQuery repositoryQuery = CrateRepositoryQuery.buildFromAnnotation(repositoryMethod, mock(CrateOperations.class));
        assertThat(repositoryQuery, is(instanceOf(CrateRepositoryQuery.class)));
        assertThat(((CrateRepositoryQuery) repositoryQuery).getSource(), is("select * from sys.nodes"));
    }

    interface AnnotatedQueryRepository {

        @Query("select * from sys.nodes")
        List<SampleEntity> selectFromNodes();
    }

    private CrateQueryMethod prepareQueryMethod(String methodName, Class<?> entityClass) throws Exception {
        RepositoryMetadata repositoryMetadata = Mockito.mock(RepositoryMetadata.class);
        when(repositoryMetadata.getDomainType()).thenReturn((Class) entityClass);

        Method testMethod = AnnotatedQueryRepository.class.getMethod(methodName);
        when(repositoryMetadata.getReturnedDomainClass(testMethod)).thenReturn((Class) entityClass);

        return new CrateQueryMethod(testMethod, repositoryMetadata, mock(ProjectionFactory.class));
    }

}
