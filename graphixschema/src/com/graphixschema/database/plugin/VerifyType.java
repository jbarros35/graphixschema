package com.graphixschema.database.plugin;

public enum VerifyType {

	DIGIT(1), LETTER(2);
	
	private Integer type;
	
	private VerifyType(Integer type) {
		this.type = type;
	}
	
	public Integer getValue() {
		return type;
	}
}


