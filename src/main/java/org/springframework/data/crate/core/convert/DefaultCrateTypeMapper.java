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

import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.convert.TypeAliasAccessor;
import org.springframework.data.crate.core.mapping.CrateDocument;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class DefaultCrateTypeMapper extends DefaultTypeMapper<CrateDocument> implements CrateTypeMapper {
	
	public DefaultCrateTypeMapper(String typeKey) {
		super(new CrateDocumentTypeAliasAccessor(typeKey));
	}
	
	public static class CrateDocumentTypeAliasAccessor implements TypeAliasAccessor<CrateDocument> {

		private final String typeKey;
		
		private CrateDocumentTypeAliasAccessor(String typeKey) {
			super();
			this.typeKey = typeKey;
		}
		
		@Override
		public Object readAliasFrom(CrateDocument source) {
			return source.get(typeKey);
		}

		@Override
		public void writeTypeTo(CrateDocument sink, Object alias) {
			if (typeKey != null) {
				sink.put(typeKey, alias);
			}
		}		
	}
}