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
package org.springframework.data.crate.core.convert;

import static org.springframework.util.Assert.notNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.EntityInstantiators;

/**
 * Base implementation of {@link CrateConverter} providing basic functionality for {@link MappingCrateConverter}.
 * Sets up a {@link GenericConversionService} and populates basic converters while allowing registration of 
 * {@link CustomConversions}.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public abstract class AbstractCrateConverter implements CrateConverter, InitializingBean {
	
	protected final GenericConversionService conversionService;
	protected EntityInstantiators instantiators;
	protected CustomConversions conversions;

	protected AbstractCrateConverter(GenericConversionService conversionService) {
		super();
		this.conversionService = conversionService == null ? new DefaultConversionService() 
														   : conversionService;
		this.instantiators = new EntityInstantiators();
		this.conversions = new CustomConversions();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		notNull(instantiators, "An instance of EntityInstantiators is required");
		notNull(conversions, "An instance of CustomConversions is required");
		initializeConverters();
	}
	
	/**
	 * @return the conversion service.
	 */
	  @Override
	  public ConversionService getConversionService() {
	    return conversionService;
	  }
	
	/**
	 * Registers {@link EntityInstantiators} to customize entity instantiation.
	 * 
	 * @param instantiators
	 */
	public void setInstantiators(EntityInstantiators instantiators) {
		this.instantiators = instantiators;
	}
	
	/**
	 * Registers the given custom conversions with the converter.
	 * 
	 * @param conversions
	 */
	public void setCustomConversions(CustomConversions conversions) {
		this.conversions = conversions;
	}
	
	/**
	 * Registers additional converters that will be available when using the {@link ConversionService} directly.
	 */
	private void initializeConverters() {
		conversions.registerConvertersIn(conversionService);
	}
}