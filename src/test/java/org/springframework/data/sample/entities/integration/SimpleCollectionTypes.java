package org.springframework.data.sample.entities.integration;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

@Table
public class SimpleCollectionTypes {
	
	@Id
	public int id;
	public Set<String> strings;
	public List<Integer> integers;
	public boolean[] booleans;
	public String[] stringArray;
	public Long[] longs;
	public static SimpleCollectionTypes simpleCollectionTypes() {
		
		SimpleCollectionTypes entity = new SimpleCollectionTypes();
		entity.id = 1;
		entity.booleans = new boolean[]{true, false};
		entity.integers = asList(1, 2, new Integer(3));
		entity.strings = new HashSet<String>(asList("C", "R", "A", "T", "E"));
		entity.stringArray = new String[]{"C", "R", "A", "T", "E"};
		entity.longs = new Long[]{1L};
		
		return entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof SimpleCollectionTypes)) {
            return false;
        }
		
        if (this == obj) {
            return true;
        }
        
        SimpleCollectionTypes that = (SimpleCollectionTypes) obj;
        
        return new EqualsBuilder().append(this.id, that.id)
				                  .append(this.booleans, that.booleans)
				                  .append(this.integers, that.integers)
				                  .append(this.strings, that.strings)
				                  .append(this.stringArray, that.stringArray)
				                  .append(this.longs, that.longs)
				                  .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 7).append(id)
										.append(booleans)
										.append(integers)
										.append(strings)
										.append(stringArray)
										.append(longs)
										.toHashCode();
	}
}