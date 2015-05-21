package com.graphixschema.database.vo

class Column implements Serializable {

	Class type
	String name
	boolean pk
	boolean fk
	boolean nullable
	boolean autoRelated
	BigDecimal precision

	Object value = null
	public Column(Class type, String name, boolean nullable,
			BigDecimal precision, Object value) {
		super();
		this.type = type;
		this.name = name;
		this.nullable = nullable;
		this.precision = precision;
		this.value = value;
	}

	/**
	 * 
	 */
	def String toString() {
		"$name, $value=value, type=$type, null=$nullable, size=$precision"
	}	
}
