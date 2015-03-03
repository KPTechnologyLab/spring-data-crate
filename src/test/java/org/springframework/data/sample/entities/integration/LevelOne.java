package org.springframework.data.sample.entities.integration;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LevelOne {
	
	public double levelOneDoubleField;
	
	public static LevelOne levelOne() {
		
		LevelOne levelOne = new LevelOne();
		levelOne.levelOneDoubleField = 1.0D;
		
		return levelOne;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof LevelOne)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        LevelOne that = (LevelOne) obj;
        
        return new EqualsBuilder().append(this.levelOneDoubleField, that.levelOneDoubleField)
        						  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 19).append(levelOneDoubleField)
										  .toHashCode();
	}
	
	@Override
	public String toString() {
		return reflectionToString(this);
	}
}