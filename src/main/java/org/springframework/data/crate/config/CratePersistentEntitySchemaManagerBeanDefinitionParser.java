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

import static org.springframework.data.config.ParsingUtils.setPropertyValue;
import static org.springframework.data.crate.config.BeanNames.SCHEMA_EXPORT_MANAGER;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.config.BeanComponentDefinitionBuilder;
import org.springframework.data.crate.core.mapping.schema.CratePersistentEntitySchemaManager;
import static org.springframework.data.crate.core.mapping.schema.SchemaExportOption.*;
import org.w3c.dom.Element;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CratePersistentEntitySchemaManagerBeanDefinitionParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
		
		BeanComponentDefinitionBuilder helper = new BeanComponentDefinitionBuilder(element, context);
		
		String crateTemplateRef = element.getAttribute("crate-template-ref");
		String exportOptionString = element.getAttribute("export-option");
		
		BeanDefinitionBuilder schemaManagerBuilder = BeanDefinitionBuilder.genericBeanDefinition(CratePersistentEntitySchemaManager.class);
		schemaManagerBuilder.addConstructorArgReference(crateTemplateRef);
		schemaManagerBuilder.addConstructorArgValue(valueOf(exportOptionString));
		
		setPropertyValue(schemaManagerBuilder, element, "ignoreFailures", "ignoreFailures");
		schemaManagerBuilder.getBeanDefinition();
		
		
		return (AbstractBeanDefinition)helper.getComponentIdButFallback(schemaManagerBuilder,
																		SCHEMA_EXPORT_MANAGER)
											 .getBeanDefinition();
	}
}