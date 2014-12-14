package org.springframework.data.crate.config;

import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.CREATE_DROP;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.crate.core.mapping.schema.SchemaExportOption;

@Configuration
public class TestCrateConfiguration extends AbstractCrateConfiguration {
	
	@Override
	public SchemaExportOption getSchemaExportOption() {		
		return CREATE_DROP;
	}
	
	@Override
	protected String getMappingBasePackage() {
		return "org.springframework.data.sample.entities.integration";
	}
}