package org.springframework.data.sample.entities.integration;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.springframework.data.sample.entities.integration.LevelZero.levelZero;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ComplexId {
	
	public String stringField;
	public Boolean booleanField;
	public LevelZero levelZero;
	
	public static ComplexId complexId() {
		
		ComplexId id = new ComplexId();
		id.stringField = "CRATE";
		id.booleanField = TRUE;
		id.levelZero = levelZero();
		
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ComplexId)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        ComplexId that = (ComplexId) obj;
        
        return new EqualsBuilder().append(this.stringField, that.stringField)
        						  .append(this.booleanField, that.booleanField)
        						  .append(this.levelZero, that.levelZero)
        						  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 21).append(stringField)
										  .append(booleanField)
										  .append(levelZero)
										  .toHashCode();
	}
	
	@Override
	public String toString() {
		return reflectionToString(this);
	}
}