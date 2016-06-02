package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.data.sample.entities.integration.Language.language;

public class Country {

    public String name;
    public List<Language> languages;

    public static Country country() {

        Country country = new Country();
        country.name = "Australia";
        country.languages = new ArrayList<Language>(asList(language()));

        return country;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Country)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Country that = (Country) obj;

        return new EqualsBuilder().append(this.name, that.name)
                .append(this.languages, that.languages)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 19).append(name)
                .append(languages)
                .toHashCode();
    }
}
