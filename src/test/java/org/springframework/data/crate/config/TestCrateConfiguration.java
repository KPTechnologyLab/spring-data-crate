package org.springframework.data.crate.config;

import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;

@Configuration
public class TestCrateConfiguration extends AbstractCrateConfiguration {
	
	@Bean
	public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
		return new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP);
	}
	
	@Override
	protected String getMappingBasePackage() {
		return "org.springframework.data.sample.entities.integration";
	}
}