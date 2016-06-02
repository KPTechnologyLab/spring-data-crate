package org.springframework.data.sample.entities.integration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.springframework.data.sample.entities.integration.LevelOne.levelOne;

public class LevelZero {

    public Integer levelZeroIntField;
    public long levelZeroLongField;
    public LevelOne levelOne;

    public static LevelZero levelZero() {

        LevelZero levelZero = new LevelZero();
        levelZero.levelZeroIntField = 5;
        levelZero.levelZeroLongField = 4L;
        levelZero.levelOne = levelOne();

        return levelZero;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof LevelZero)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        LevelZero that = (LevelZero) obj;

        return new EqualsBuilder().append(this.levelZeroIntField, that.levelZeroIntField)
                .append(this.levelZeroLongField, that.levelZeroLongField)
                .append(this.levelOne, that.levelOne)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 21).append(levelZeroIntField)
                .append(levelZeroLongField)
                .append(levelOne)
                .toHashCode();
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
