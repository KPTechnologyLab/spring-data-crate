package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

import java.util.*;

import static java.util.Arrays.asList;

@Table(numberOfReplicas = "0")
public class SimpleMapTypes {

    @Id
    public String id;
    public Map<String, String> stringMap;
    public Map<Integer, Integer> integerMap;
    public Map<Locale, Integer> localeMap;
    public Map<String, Collection<String>> mapOfCollections;
    public Map<String, int[]> mapOfArrays;
    public Map<String, Boolean[]> mapOfBoolenWrappers;

    public static SimpleMapTypes simpleMapTypes() {

        SimpleMapTypes entity = new SimpleMapTypes();
        entity.id = "CRATE";
        entity.integerMap = new HashMap<Integer, Integer>();
        entity.integerMap.put(1, 2);
        entity.localeMap = new LinkedHashMap<Locale, Integer>();
        entity.localeMap.put(Locale.CANADA, 1);
        entity.mapOfArrays = new HashMap<String, int[]>();
        entity.mapOfArrays.put("CRATE", new int[]{1});
        entity.mapOfCollections = new HashMap<String, Collection<String>>();
        entity.mapOfCollections.put("List", asList("CRATE"));
        entity.stringMap = new LinkedHashMap<String, String>();
        entity.stringMap.put("KEY", "VALUE");
        entity.mapOfBoolenWrappers = new HashMap<String, Boolean[]>();
        entity.mapOfBoolenWrappers.put("Key", new Boolean[]{true});

        return entity;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof SimpleMapTypes)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        SimpleMapTypes that = (SimpleMapTypes) obj;

        return new EqualsBuilder().append(this.id, that.id)
                .append(this.stringMap, that.stringMap)
                .append(this.integerMap, that.integerMap)
                .append(this.localeMap, that.localeMap)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 15).append(id)
                .append(stringMap)
                .append(integerMap)
                .append(localeMap)
                .toHashCode();
    }
}
