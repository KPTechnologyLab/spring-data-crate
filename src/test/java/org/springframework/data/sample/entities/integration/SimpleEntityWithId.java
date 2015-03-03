package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table(name="sub_class")
public class SimpleEntityWithId extends SimpleEntity {
	
	@Id
	public long id;
	
	public static SimpleEntityWithId simpleEntityWithId() {
		
		SimpleEntity simpleEntity = simpleEntity();
		SimpleEntityWithId entity = new SimpleEntityWithId();
		entity.id = 1;
		entity.integerField = 2;
		entity.boolField = simpleEntity.boolField;
    	entity.dateField = simpleEntity.dateField;
    	entity.localeField = simpleEntity.localeField;
    	entity.stringField= simpleEntity.stringField;
    	
    	return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof SimpleEntityWithId)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        SimpleEntityWithId that = (SimpleEntityWithId) obj;
        
        return new EqualsBuilder().append(this.id, that.id)
        						  .append(this.stringField, that.stringField)
				                  .append(this.integerField, that.integerField)
				                  .append(this.dateField, that.dateField)
				                  .append(this.boolField, that.boolField)
				                  .append(this.localeField, that.localeField)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(7, 11).append(id)
										 .append(stringField)
										 .append(integerField)
										 .append(dateField)
										 .append(boolField)
										 .append(localeField)
										 .toHashCode();
	}
}