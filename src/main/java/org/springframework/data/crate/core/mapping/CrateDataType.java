package org.springframework.data.crate.core.mapping;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrateDataType {
	
	public static final String BOOLEAN = "boolean";
	public static final String STRING = "string";
	public static final String INTEGER = "integer";
	public static final String LONG = "long";
	public static final String SHORT = "short";
	public static final String DOUBLE = "double";
	public static final String FLOAT = "float";
	public static final String BYTE = "byte";
	public static final String TIMESTAMP = "timestamp";
	public static final String ARRAY = "array";
	public static final String OBJECT = "object";
	
	private static final Map<Class<?>, String> CRATE_TYPES = new HashMap<Class<?>, String>();
	
	static {
		CRATE_TYPES.put(Boolean.class, BOOLEAN);
		CRATE_TYPES.put(Boolean.TYPE, BOOLEAN);
		CRATE_TYPES.put(String.class, STRING);
		CRATE_TYPES.put(Byte.class, BYTE);
		CRATE_TYPES.put(Byte.TYPE, BYTE);
		CRATE_TYPES.put(Short.class, SHORT);
		CRATE_TYPES.put(Short.TYPE, SHORT);
		CRATE_TYPES.put(Integer.class, INTEGER);
		CRATE_TYPES.put(Integer.TYPE, INTEGER);
		CRATE_TYPES.put(Long.class, LONG);
		CRATE_TYPES.put(Long.TYPE, LONG);
		CRATE_TYPES.put(Float.class, FLOAT);
		CRATE_TYPES.put(Float.TYPE, FLOAT);
		CRATE_TYPES.put(Double.TYPE, DOUBLE);
		CRATE_TYPES.put(Double.class, DOUBLE);
		CRATE_TYPES.put(Date.class, TIMESTAMP);
	}
	
	private CrateDataType() {
	}
	
	public static String getCrateTypeFor(Class<?> clazz) {
		
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
			return ARRAY;
		}
		
		if(Map.class.isAssignableFrom(clazz)) {
			return OBJECT;
		}
		
		return CRATE_TYPES.get(clazz) == null ? OBJECT : CRATE_TYPES.get(clazz);
	}
}