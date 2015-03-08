package org.springframework.data.sample.entities.integration;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.springframework.data.sample.entities.integration.ComplexId.complexId;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table(numberOfReplicas="0")
public class EntityWithComplexId {
	
	@Id
	public ComplexId complexId;
	public String type;
	
	public static EntityWithComplexId entityWithComplexId() {
		
		EntityWithComplexId entity = new EntityWithComplexId();
		entity.complexId = complexId();
		entity.type = "complex";
		
		return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof EntityWithComplexId)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        EntityWithComplexId that = (EntityWithComplexId) obj;
        
        return new EqualsBuilder().append(this.complexId, that.complexId)
        						  .append(this.type, that.type)
        						  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 21).append(complexId)
										  .append(type)
										  .toHashCode();
	}
	
	@Override
	public String toString() {
		return reflectionToString(this);
	}
}