package org.springframework.data.sample.entities.integration;

import static org.springframework.data.sample.entities.integration.SimpleEntity.simpleEntity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table
public class ObjectMapTypes {
	
	@Id
	public long id;
	public Map<String, SimpleEntity> map;
	
	public static ObjectMapTypes objectMapTypes() {
		
		ObjectMapTypes entity = new ObjectMapTypes();
		entity.id = 1L;
		entity.map = new LinkedHashMap<String, SimpleEntity>();
		entity.map.put("se1", simpleEntity());
		
		return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ObjectMapTypes)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        ObjectMapTypes that = (ObjectMapTypes) obj;
        
        return new EqualsBuilder().append(this.id, that.id)
				                  .append(this.map, that.map)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 15).append(id)
										.append(map)
										.toHashCode();
	}
}