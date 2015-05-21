package com.graphixschema.database;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.graphixschema.database.vo.ConnectionProperties;
import com.graphixschema.database.vo.NullObject;


public class OracleConnection {

	private static Map<String, Class> typesMap = new HashMap();
	public static final Map<String, Boolean> nullMap = new HashMap();
	private ConnectionProperties connProperties;
	
	static {
		nullMap.put("Y", true);
		nullMap.put("N", false);
		typesMap.put("VARCHAR", String.class);
		typesMap.put("VARCHAR2", String.class);
		typesMap.put("NUMBER", Long.class);
		typesMap.put("DATE", Date.class);
		typesMap.put("TIMESTAMP", Timestamp.class);
		typesMap.put("CHAR", Character.class);
	}

	Connection conn = null;
	Statement st = null;

	/**
	 * 
	 */
	public void openConnection() {
		try {
			String url = connProperties.getUrl();
			String user = connProperties.getUser();
			String password = connProperties.getPassword();
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(url, user, password);
			//conn.setAutoCommit(false);
			st = conn.createStatement();
		} catch (Exception e) {
			System.out
					.println("Error en la conexión de la base de datos, "
							+ "revisar el archivo connection.properties y comprobar si en"
							+ " el mismo camino de. jar. \n" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void openConnection(ConnectionProperties connProperties) throws Exception {
		try {
			this.connProperties = connProperties;
			String url = connProperties.getUrl();
			String user = connProperties.getUser();
			String password = connProperties.getPassword();
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(url, user, password);
			st = conn.createStatement();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * @param connProperties
	 * @throws SQLException 
	 */
	public void testConnection(ConnectionProperties connProperties) throws SQLException {		
		String url = connProperties.getUrl();
		String user = connProperties.getUser();
		String password = connProperties.getPassword();
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		conn = DriverManager.getConnection(url, user, password);
		st = conn.createStatement();
		closeConnection();
	}

	public ConnectionProperties getConnProperties() {
		return connProperties;
	}

	public void setConnProperties(ConnectionProperties connProperties) {
		this.connProperties = connProperties;
	}

	/**
	 * @param sqlString
	 * @return
	 */
	public ResultSet executeQuery(String sqlString) {
		ResultSet results = null;
		try {
			if (conn != null && st != null) {
				results = st.executeQuery(sqlString);
			} else {
				throw new IllegalArgumentException();
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return results;
	}
	
	/**
	 * @param sqlString
	 * @return
	 */
	public int executeUpdate(String sqlString) {
		int results = -1;
		try {
			if (conn != null && st != null) {
				results = st.executeUpdate(sqlString);
			} else {
				throw new IllegalArgumentException();
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return results;
	}

	/**
	 * 
	 */
	public void closeConnection() {
		if (conn != null) {
			try {
				conn.commit();
				st.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Convert a database datatype into a Java type
	 * 
	 * @param type
	 *            datatype
	 * @return Java type
	 */
	public Class<? extends Object> convert2Java(int type) {
		switch (type) {
		case java.sql.Types.ARRAY:
		case java.sql.Types.BIT:
		case java.sql.Types.DATALINK:
		case java.sql.Types.DISTINCT:
		case java.sql.Types.NULL:
		case java.sql.Types.OTHER:
		case java.sql.Types.REF:
		case java.sql.Types.SQLXML:
		case java.sql.Types.STRUCT:
			throw new IllegalArgumentException("The Type " + type
					+ " is not supportet for conversion to a java type");
		case java.sql.Types.ROWID:
		case java.sql.Types.BIGINT:
			return BigInteger.class;
		case java.sql.Types.BINARY:
		case java.sql.Types.BLOB:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.VARBINARY:
			return Blob.class;
		case java.sql.Types.BOOLEAN:
			return Boolean.class;
		case java.sql.Types.CLOB:
		case java.sql.Types.LONGNVARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.NCHAR:
		case java.sql.Types.NCLOB:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.VARCHAR:
			return String.class;
		case java.sql.Types.DATE:
			return Date.class;
		case java.sql.Types.NUMERIC:
			return Long.class;
		case java.sql.Types.DECIMAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.REAL:
			return Double.class;
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			return Integer.class;
		case java.sql.Types.JAVA_OBJECT:
			return Object.class;
		case java.sql.Types.TIME:
			return Time.class;
		case java.sql.Types.TIMESTAMP:
			return Timestamp.class;
		case java.sql.Types.CHAR:
			return Character.class;
		default:
			throw new IllegalArgumentException("Unknown Type " + type);
		}
	}

	/**
	 * @return
	 */
	public DatabaseMetaData getMetaData() {
		DatabaseMetaData metaData = null;
		if (conn != null) {
			try {
				metaData = conn.getMetaData();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return metaData;
	}

	/**
	 * @return
	 */
	public String getCatalog() {
		String catalog = null;
		if (conn != null) {
			try {
				catalog = conn.getCatalog();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return catalog;
	}

	/**
	 * @param oracleType
	 * @return
	 */
	public Class convert2Java(String oracleType, int scale) {
		if (oracleType.indexOf("(") != -1) {
			oracleType = oracleType.substring(0, oracleType.indexOf("("));
		}
		Class javaType = typesMap.get(oracleType);
		if (javaType == null) {
			//System.out.println("Tipo desconocido: " + oracleType);
			javaType = NullObject.class;
		}
		if (javaType.equals(Long.class) && scale > 0) {
			javaType = Double.class;
		}
		return javaType;
	}

	/**
	 * 
	 */
	public void rollback() {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}	
	
}
