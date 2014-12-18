/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.data.crate.core.mapping.event;

import org.springframework.data.annotation.Id;
import org.springframework.data.crate.core.mapping.annotations.Table;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 */
@Table
public class User {

	@Id
	private String id;

	@Size(min=10)
	private String name;

	@Min(18)
	private Integer age;

	public User(String id, String name, Integer age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getAge() {
		return age;
	}
}