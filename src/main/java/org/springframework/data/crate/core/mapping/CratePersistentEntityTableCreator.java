/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.data.crate.core.mapping;

import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.notNull;
import io.crate.client.CrateClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.MappingContextEvent;

/**
 * Component that inspects {@link CratePersistentEntity} instances contained in the given {@link CrateMappingContext}
 * for creating tables in Crate DB.
 *
 * @author Hasnain Javed
 * 
 * @since 1.0.0
 */
public class CratePersistentEntityTableCreator implements ApplicationListener<MappingContextEvent<CratePersistentEntity<?>, CratePersistentProperty>> {
	
	private final Logger logger;
	private final Map<Class<?>, Boolean> inspectedEntities;
	private CrateMappingContext mappingContext;
	private CrateClient client;
	private TableDefinitionMapper mapper;
	
	/**
	 * Creats a new {@link CratePersistentEntityTableCreator} for the given {@link CrateMappingContext} and
	 * {@link CrateClient}.
	 * 
	 * @param mappingContext must not be {@literal null}.
	 * @param client must not be {@literal null}.
	 */	
	public CratePersistentEntityTableCreator(CrateMappingContext mappingContext, CrateClient client) {
		this(mappingContext, client, new CratePersistentEntityTableDefinitionMapper(mappingContext));
	}
	
	/**
	 * Creats a new {@link CratePersistentEntityTableCreator} for the given {@link CrateMappingContext},
	 * {@link CrateClient} and {@link TableDefinitionMapper}.
	 * 
	 * @param mappingContext must not be {@literal null}.
	 * @param client must not be {@literal null}.
	 * @param mapper must not be {@literal null}.
	 */	
	public CratePersistentEntityTableCreator(CrateMappingContext mappingContext, CrateClient client, TableDefinitionMapper mapper) {
		
		super();
		
		notNull(mappingContext);
		notNull(client);
		notNull(mapper);
		
		logger = getLogger(getClass());
		
		this.mappingContext = mappingContext;
		this.client = client;
		this.mapper = mapper;
		
		inspectedEntities = new ConcurrentHashMap<Class<?>, Boolean>();
		
		for(CratePersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
			createTable(entity);
		}
	}
	
	@Override
	public void onApplicationEvent(MappingContextEvent<CratePersistentEntity<?>, CratePersistentProperty> event) {
		
		if (!event.wasEmittedBy(mappingContext)) {
			return;
		}
		
		PersistentEntity<?, ?> entity = event.getPersistentEntity();

		// Double check type as Spring infrastructure does not consider nested generics
		if (entity instanceof CratePersistentEntity) {
			createTable(event.getPersistentEntity());
		}
	}
	
	/**
	 * Returns whether the current table creator was registered for the given {@link MappingContext}.
	 * 
	 * @param context 
	 */
	public boolean isTableCreatorFor(MappingContext<?, ?> context) {
		return this.mappingContext.equals(context);
	}
	
	private void createTable(CratePersistentEntity<?> entity) {
		
		Class<?> type = entity.getType();

		if (!inspectedEntities.containsKey(type)) {
			inspectedEntities.put(type, TRUE);
		}
		
		logger.debug("inspecting class {} for table information", type);
		
		if(entity.findAnnotation(Table.class) != null) {
			TableDefinition table = mapper.createDefinition(entity);
			logger.info("{}", table);
			client.sql("");
		}else {
			logger.warn("class {} is not annotated with {}", type, Table.class);
		}
	}
}