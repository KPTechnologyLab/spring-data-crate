package org.springframework.data.sample.entities.integration;

import static java.util.Arrays.asList;
import static org.springframework.data.sample.entities.integration.Address.address;
import static org.springframework.data.sample.entities.integration.Email.email;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table(name="person")
public class Person {
	
	@Id
	public String id;
	public String name;
	public Address address;
	public Set<Email> emails;
	
	public static Person person() {
		
		Person entity = new Person();
		entity.id = "abcxyz";
		entity.name = "CRATE";
		entity.address = address();
		entity.emails = new HashSet<Email>(asList(email()));
		
		return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Person)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        Person that = (Person) obj;
        
        return new EqualsBuilder().append(this.id, that.id)
				                  .append(this.name, that.name)
				                  .append(this.address, that.address)
				                  .append(this.emails, that.emails)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(3, 9).append(id)
										.append(name)
										.append(address)
										.append(emails)
										.toHashCode();
	}
}