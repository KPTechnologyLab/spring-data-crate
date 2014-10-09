package org.springframework.data.crate.core.mapping;

/**
 * {@link TableDefinitionMapper} creates crate specific table definition for a given class.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
interface TableDefinitionMapper {
	
	TableDefinition createDefinition(CratePersistentEntity<?> entity);
}