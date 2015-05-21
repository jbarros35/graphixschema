package com.graphixschema.database.vo;

public enum XSDType {

	INT("int", "java.lang.Integer"), LONG("long", "java.lang.Long"), 
		DATE("date", "java.util.Date"), DATETIME("dateTime", "java.util.Date"), 
		STRING("string", "java.lang.String"), DOUBLE("double","java.lang.Double"), BOOLEAN("boolean", "java.lang.Boolean");

	private String type;
	private String javaType;

	private XSDType(String type) {
		this.type = type;
	}
		
	private XSDType(String type, String javaType) {
		this.type = type;
		this.javaType = javaType;
	}

	public String getValue() {
		return type;
	}

	/**
	 * @param text
	 * @return
	 */
	public static XSDType fromString(String text) {
		if (text != null) {
			for (XSDType b : XSDType.values()) {
				if (text.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param javaType
	 * @return
	 */
	public static XSDType javaToXSDType(String javaType) {
		if (javaType != null) {
			for (XSDType b : XSDType.values()) {
				if (javaType.equalsIgnoreCase(b.javaType)) {
					return b;
				}
			}
		}
		return null;
	}
}
