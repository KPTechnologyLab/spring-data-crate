package org.springframework.data.sample.entities.integration;

import static org.springframework.data.sample.entities.integration.Country.country;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Address {
	
	public Country country;
	public String street;
	public String city;
	
	public static Address address() {
		
		Address address = new Address();
		address.city = "Melbourne";
		address.street = "Sesamestreet";
		address.country = country();
		
		return address;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Address)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        Address that = (Address) obj;
        
        return new EqualsBuilder().append(this.country, that.country)
				                  .append(this.city, that.city)
				                  .append(this.street, that.street)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 21).append(country)
										  .append(city)
										  .append(street)
										  .toHashCode();
	}
}