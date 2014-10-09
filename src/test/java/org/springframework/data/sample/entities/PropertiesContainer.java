package org.springframework.data.sample.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertiesContainer {
	
	public String simpleField;
	public Book compositeField;
	public List<String> listField;
	public Set<Integer> setField;
	public Book[] arrayField;
	public Map<String, String> mapField;
}