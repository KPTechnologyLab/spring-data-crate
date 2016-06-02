package org.springframework.data.crate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;

import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;

public class TestCrateConfiguration extends AbstractCrateConfiguration {

    @Bean
    public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
        return new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP);
    }

}
