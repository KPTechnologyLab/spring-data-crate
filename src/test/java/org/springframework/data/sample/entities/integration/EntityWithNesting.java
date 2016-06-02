package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.crate.core.mapping.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.springframework.data.sample.entities.integration.Country.country;

@Table(numberOfReplicas = "0")
public class EntityWithNesting {

    @Id
    public Long id;
    @Version
    public Long version;
    public String name;
    public Country country;
    public Map<String, String> map;
    public List<Integer> integers;

    public static EntityWithNesting entityWithNestingAndSimpleId() {

        Map<String, String> m = new HashMap<String, String>();
        m.put("Key_1", "Value_1");

        List<Integer> list = new ArrayList<Integer>(asList(1, 2));

        EntityWithNesting entity = new EntityWithNesting();
        entity.id = 1L;
        entity.name = "CRATE";
        entity.country = country();
        entity.map = m;
        entity.integers = list;

        return entity;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof EntityWithNesting)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        EntityWithNesting that = (EntityWithNesting) obj;

        return new EqualsBuilder().append(this.id, that.id)
                .append(this.name, that.name)
                .append(this.country, that.country)
                .append(this.map, that.map)
                .append(this.integers, that.integers)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 25).append(id)
                .append(name)
                .append(country)
                .append(map)
                .append(integers)
                .toHashCode();
    }
}
