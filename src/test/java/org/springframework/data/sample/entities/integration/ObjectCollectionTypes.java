package org.springframework.data.sample.entities.integration;

import static java.util.Arrays.asList;
import static org.springframework.data.sample.entities.integration.SimpleEntity.simpleEntity;
import static org.springframework.data.sample.entities.integration.SimpleEntityWithId.simpleEntityWithId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table(name="obj_collection", numberOfReplicas="0")
public class ObjectCollectionTypes {
	
	@Id
	public String id;
	public List<SimpleEntity> simpleEntities;
	public Set<SimpleEntityWithId> simpleEntityWithIds;
	public SimpleEntity[] simpleEntitiesArray;
	
	public static ObjectCollectionTypes objectCollectionTypes() {
		
		ObjectCollectionTypes entity = new ObjectCollectionTypes();
		
		entity.id = "CRATE";
		entity.simpleEntities = asList(simpleEntity());
		entity.simpleEntityWithIds = new HashSet<SimpleEntityWithId>(asList(simpleEntityWithId()));
		entity.simpleEntitiesArray = new SimpleEntity[]{simpleEntity()};
		
		return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ObjectCollectionTypes)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        ObjectCollectionTypes that = (ObjectCollectionTypes) obj;
        
        return new EqualsBuilder().append(this.id, that.id)
				                  .append(this.simpleEntities, that.simpleEntities)
				                  .append(this.simpleEntityWithIds, that.simpleEntityWithIds)
				                  .append(this.simpleEntitiesArray, that.simpleEntitiesArray)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(9, 11).append(id)
										 .append(simpleEntities)
										 .append(simpleEntitiesArray)
										 .append(simpleEntityWithIds)
										 .toHashCode();
	}
}