package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Version;
import org.springframework.data.crate.core.mapping.annotations.Table;

import java.util.Date;
import java.util.Locale;

@Table(numberOfReplicas = "0")
public class SimpleEntity {

    @Version
    public Long version;
    public String stringField;
    public int integerField;
    public Date dateField;
    public Boolean boolField;
    public Locale localeField;

    public static SimpleEntity simpleEntity() {

        SimpleEntity entity = new SimpleEntity();
        entity.boolField = true;
        entity.dateField = new Date();
        entity.localeField = Locale.CANADA;
        entity.stringField = "CRATE";

        return entity;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof SimpleEntity)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        SimpleEntity that = (SimpleEntity) obj;

        return new EqualsBuilder().append(this.stringField, that.stringField)
                .append(this.integerField, that.integerField)
                .append(this.dateField, that.dateField)
                .append(this.boolField, that.boolField)
                .append(this.localeField, that.localeField)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 9).append(stringField)
                .append(integerField)
                .append(dateField)
                .append(boolField)
                .append(localeField)
                .toHashCode();
    }
}
