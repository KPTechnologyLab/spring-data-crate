package org.springframework.data.crate.core.mapping.schema;

import static org.springframework.util.Assert.hasText;

import org.springframework.util.StringUtils;

class ColumnMetadata {
	
	private String sqlPath;
	private String crateType;
	private String elementCrateType;
	
	public ColumnMetadata(String sqlPath, String crateType) {
		this(sqlPath, crateType, null);
	}
	
	public ColumnMetadata(String sqlPath, String crateType, String elementCrateType) {
		super();
		hasText(sqlPath);
		hasText(crateType);
		this.sqlPath = sqlPath;
		this.crateType = crateType;
		
		if(StringUtils.hasText(elementCrateType)) {
			this.elementCrateType = elementCrateType;
		}
	}

	public String getSqlPath() {
		return sqlPath;
	}

	public String getCrateType() {
		return crateType;
	}

	public String getElementCrateType() {
		return elementCrateType;
	}

	@Override
	public String toString() {
		return "[name: ".concat(sqlPath)
						.concat(", crateType: "
						.concat(crateType)
						.concat("]"));
	}
}