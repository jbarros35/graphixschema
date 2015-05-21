package com.graphixschema.database.graph

import java.sql.Timestamp;
import java.text.SimpleDateFormat

import com.graphixschema.database.vo.Column;
import com.graphixschema.database.vo.NullObject;

class GenerateValue {

	private static final int MAX_CARACTERES = 50
	
	//simple nullmap
	static Map<String,Boolean> nullMap = ["Y":true, "N":false]
	
	//types map
	def static typesMap = ["VARCHAR": String.class,"VARCHAR2": String.class, "NUMBER": Long.class, "DATE": Date.class,
		"TIMESTAMP": Timestamp.class, "CHAR": Character.class]
		
	def static String[] stringValues = [
		"A","B","C","D","E","F",
		"G","H","I","J","K","L",
		"M","N","O","P","Q","R",
		"S","T","U", "V", "X","Z", " ",
		"a", "b", "c", "d",	"e", "f", "g", "h",	"i", "j", "k", "l", 
		"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "x", "y", "z", 
		"@", "#", "%", "*", ".",
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
	]

	BigDecimal precision

	/**
	 * 
	 * @return
	 */
	def createColumn(columName, type, scale, nullAble, precision) {
		def classType = convert2Java(type, scale)
		def value = getValue(classType)		
				
		Column col = new Column(classType, columName, nullAble, precision, value)
	}	
	
	/**
	 * 
	 * @param oracleType
	 * @param scale
	 * @return
	 */
	Class convert2Java(String oracleType, int scale) {
		if (oracleType.indexOf("(") != -1) {
			oracleType = oracleType.substring(0, oracleType.indexOf("("))
		}
		Class javaType = typesMap.get(oracleType)
		if (javaType == null) {
			javaType = NullObject.class
		}
		if (javaType.equals(Long.class) && scale > 0) {
			javaType = Double.class
		}
		javaType
	}
	
	
	void setRange(BigDecimal precision) {
		this.precision = precision
	}

	/**
	 * @return value
	 */
	def getValue = {Class type->
		Object value = null
		if (type.equals(String.class)) {
			value = getString(precision.intValue())
		} else if (type.equals(Date.class)) {
			value = getDate(new Date())
		} else if (type.equals(Timestamp.class)) {
			value = getTimeStamp(new Date())
		} else if (type.equals(Character.class)) {
			value = getChar()
		} else if (type.equals(Long.class)) {
			value = getLong()
		} else if (type.equals(Double.class)) {
			value = getDouble()
		} else if (type.equals(NullObject.class)) {
			value = getNull()
		}
		value
	}

	private Object getNull() {

		return "NULL"
	}

	/**
	 * TO_TIMESTAMP('21/02/2014 17:59:13.273000','DD/MM/YYYY fmHH24fm:MI:SS.FF')
	 * @param date
	 * @return
	 */
	private Object getTimeStamp(Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSSSS")
		StringBuffer buff = new StringBuffer()
		buff.append("TO_TIMESTAMP('")
		buff.append(sf.format(date))
		buff.append("', 'DD/MM/YYYY fmHH24fm:MI:SS.FF')")
		buff.toString()
	}

	/**
	 * TO_DATE('12/26/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS')
	 * @return
	 */
	String getDate(Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
		StringBuffer buff = new StringBuffer();
		buff.append("TO_DATE('");
		buff.append(sf.format(date));
		buff.append("', 'DD/MM/YYYY HH24:MI:SS')");
		buff.toString();
	}

	/**
	 * No maximo MAX_CARACTERES caracteres.
	 * @return
	 */
	String getString(int range) {
		if (range > MAX_CARACTERES) {
			range = MAX_CARACTERES;
		}
		StringBuffer buff = new StringBuffer();
		Random random = new Random();
		buff.append("'");
		for (int i = 0; i < range; i++) {
			int index = random.nextInt(stringValues.length);
			String value = stringValues[index];
			buff.append(value);
		}
		buff.append("'");
		buff.toString();
	}

	/**
	 * @return
	 */
	String getChar() {
		StringBuffer buff = new StringBuffer();
		Random random = new Random();
		int index = 0;
		if (random.nextBoolean()) {
			index = 1;
		}
		buff.append("'");
		buff.append(index);
		buff.append("'");
		buff.toString();
	}

	/**
	 * @return
	 */
	String getLong() {
		StringBuffer buff = new StringBuffer();
		Random random = new Random();
		Long value = random.nextLong();
		if (value < 0) {
			value *= -1;
		}
		buff.append(value);
		int range = precision.precision();
		if (buff.length() > range) {
			buff.setLength(range);
			buff.trimToSize();
		}
		buff.toString();
	}

	/**
	 * @return
	 */
	String getDouble() {
		StringBuffer buff = new StringBuffer();
		int intPart = precision.intValue();
		int decimalPart = precision.scale();
		intPart -= decimalPart;
		StringBuffer part1 = new StringBuffer();
		for (int i = 0; i < intPart; i++){
			part1.append(getRandomInt(9));
		}
		StringBuffer part2 = new StringBuffer();
		for (int i = 0; i < decimalPart; i++){
			part2.append(getRandomInt(9));
		}
		buff.append(part1.append(".").append(part2));
		buff.toString();
	}

	/**
	 * @param range
	 * @return
	 */
	Integer getRandomInt(int range) {
		Random random = new Random();
		int value = random.nextInt(range);
		value;
	}

}
