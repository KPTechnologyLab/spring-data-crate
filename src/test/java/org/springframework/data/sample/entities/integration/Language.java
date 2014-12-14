package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Language {
	
	public String name;
	
	public static Language language() {
		
		Language language = new Language();
		language.name = "JAVA";
		
		return language;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Language)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        Language that = (Language) obj;
        
        return new EqualsBuilder().append(this.name, that.name).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 17).append(name).toHashCode();
	}
}