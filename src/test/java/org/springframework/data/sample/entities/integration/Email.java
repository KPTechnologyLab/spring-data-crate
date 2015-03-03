package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Email {
	
	public String email;
	
	public static Email email() {
		
		Email email = new Email();
		email.email = "sdcrate@crate.io";
		
		return email;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Email)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        Email that = (Email) obj;
        
        return new EqualsBuilder().append(this.email, that.email).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(21, 23).append(email).toHashCode();
	}
}