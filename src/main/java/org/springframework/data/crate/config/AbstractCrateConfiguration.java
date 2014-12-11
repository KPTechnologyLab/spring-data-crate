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
package org.springframework.data.crate.config;

import static java.util.Collections.emptyList;
import static org.springframework.util.StringUtils.hasText;
import io.crate.client.CrateClient;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.crate.core.CrateTemplate;
import org.springframework.data.crate.core.convert.CustomConversions;
import org.springframework.data.crate.core.convert.MappingCrateConverter;
import org.springframework.data.crate.core.mapping.CrateMappingContext;
import org.springframework.data.crate.core.mapping.annotations.Table;
import org.springframework.util.ClassUtils;

/**
 * Base class for Spring Data Crate configuration using JavaConfig.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@Configuration
public abstract class AbstractCrateConfiguration {
	
	/**
	 * Creates a {@link CrateTemplate}
	 * @return
	 */
	@Bean
	public CrateTemplate crateTemplate() throws Exception {
		return new CrateTemplate(crateClient(), mappingCrateConverter());
	}

	/**
	 * Creates a {@link CrateClient} to be used by {@link CrateTemplate}
	 * @return
	 */
	@Bean
	public CrateClient crateClient() {
		return new CrateClient("localhost:4300");
	}
	
	/**
	 * Creates a {@link MappingCrateConverter} using the configured {@link #crateMappingContext()}.
	 * Will get {@link #customConversions()} applied.
	 * @return
	 */
	@Bean
	public MappingCrateConverter mappingCrateConverter() throws Exception {
		MappingCrateConverter converter = new MappingCrateConverter(crateMappingContext());
		converter.setCustomConversions(customConversions());
		return converter;
	}
	
	/**
	 * Creates a {@link CrateMappingContext} equipped with entity classes scanned from the mapping base package.
	 * 
	 * @see #getMappingBasePackage()
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Bean
	public CrateMappingContext crateMappingContext() throws ClassNotFoundException {
		CrateMappingContext mappingContext = new CrateMappingContext();
		mappingContext.setInitialEntitySet(getInitialEntitySet());
		mappingContext.setSimpleTypeHolder(customConversions().getSimpleTypeHolder());
		return mappingContext;
	}

	/**
	 * Register custom Converters in a {@link CustomConversions} object if required. These
	 * {@link CustomConversions} will be registered with {@link #crateMappingContext()}. 
	 * Returns an empty {@link CustomConversions} instance by default.
	 * @return must not be {@literal null}.
	 */
	@Bean
	public CustomConversions customConversions() {
		return new CustomConversions(emptyList());
	}
	
	/**
	 * Scans the mapping base package for classes annotated with {@link Table}.
	 * 
	 * @see #getMappingBasePackage()
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {

		String basePackage = getMappingBasePackage();
		Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();

		if (hasText(basePackage)) {
			ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(Table.class));
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

			for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
				initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), AbstractCrateConfiguration.class.getClassLoader()));
			}
		}

		return initialEntitySet;
	}
	
	/**
	 * Return the base package to scan for mapped {@link Table}s. Will return the package name of the configuration
	 * class' (the concrete class, not this one here) by default. So if you have a {@code com.acme.AppConfig} extending
	 * {@link AbstractCrateConfiguration} the base package will be considered {@code com.acme} unless the method is
	 * overriden to implement alternate behaviour.
	 * 
	 * @return the base package to scan for mapped {@link Table} classes or {@literal null} to not enable scanning for
	 *         entities.
	 */
	protected String getMappingBasePackage() {
		return getClass().getPackage() == null ? null : getClass().getPackage().getName();
	}
}