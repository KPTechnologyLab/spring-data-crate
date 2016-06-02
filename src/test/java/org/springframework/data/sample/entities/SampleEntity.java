/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.sample.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.crate.core.mapping.annotations.Table;

/**
 * @author Rizwan Idrees
 * @author Hasnain Javed
 */
@Table
public class SampleEntity {

    @Id
    private Integer id;
    private String message;
    @Version
    private Long version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SampleEntity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        SampleEntity rhs = (SampleEntity) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.message, rhs.message)
                .append(this.version, rhs.version).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(message)
                .append(version)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "SampleEntity{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", version=" + version +
                '}';
    }
}
