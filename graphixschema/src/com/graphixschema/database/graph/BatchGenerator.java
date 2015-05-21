package com.graphixschema.database.graph;

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.graphixschema.database.OracleConnection;
import com.graphixschema.database.utilities.MemoryCache;
import com.graphixschema.database.vo.Column;
import com.graphixschema.database.vo.ConnectionProperties;


/**
 * 
 * @author jlopesde
 *
 */
public class BatchGenerator {
	
	private List values;
	DBForestLoader loader;
	Set<String> dependencies;
	Integer recordQuantity;
	private ConnectionProperties connProps;
	private OracleConnection connection;
	Set<Integer> tempPkList;
	
	public BatchGenerator(DBaseRuleLoader generator) {
		super();
		// create an instance of DBForestLoader to integrate with prolog
		this.loader = new DBForestLoader();
		loader.loadForest((File) generator.getFile());
		// load and clone generator values
		this.values = new ArrayList(generator.getColumns());
	}
	/**
	 * 
	 */
	private void connect() {
		connection = new OracleConnection();
		connection.setConnProperties(connProps);
		connection.openConnection();
	}

	/**
	 * 
	 */
	public void startBatch() throws Exception {
		try {
			connect();
			Date init = new Date();
			System.out.println("Inicio del batch: "+init);
			for (int i = 0; i < recordQuantity; i++) {
				// clean temporary fklist for new cycle
				tempPkList = new LinkedHashSet<Integer>();
				executeCycle();				
				// start updating fks in the cycle.
				updatePKValues();
			}
			Date finish = new Date();
			TimeDuration duration = TimeCategory.minus(finish, init);
			System.out.println("Fim del batch: "+finish+" tiempo de ejecucion: " +duration.getMinutes()+"m" +":"+ duration.getSeconds() +"s");
		} catch (Exception e) {
			connection.rollback();
			throw e;
		} finally {
			connection.closeConnection();	
		}
		
	}
	
	/**
	 * Iterate over treeMerged and return sql buffer.
	 * @return
	 */
	private void executeCycle() {		
		Iterator<String> itTree = dependencies.iterator();
		// iterate over nodes merged.
		while (itTree.hasNext()) {	
			StringBuilder buffer = new StringBuilder();
			final String tableName = itTree.next();
			buffer.append("INSERT INTO ");
			buffer.append(tableName);
			Map<String, String> cols = getCols(tableName);
			// get columns
			buffer.append("(");
			buffer.append(cols.get("names"));
			buffer.append(")");
			buffer.append(" VALUES (");			
			// replace the values
			buffer.append(cols.get("values"));
			buffer.append(")");
			//buffer.append("\n");
			connection.executeUpdate(buffer.toString());
		}		
	}

	/**
	 * Return a string concat with fk and cols names.
	 * @param tableName
	 */
	private Map<String, String> getCols(String tableName) {
		StringBuilder colsValues = new StringBuilder();
		StringBuilder colsBuff = new StringBuilder();
		// get first the fks set
		Set<String> fkSet = getFKSet(tableName);
		// get list of pks must be removed with there are some pks at fkset
		Set<String> pkSet = getPKSet(tableName);
		// remove fks into pk set.
		pkSet.removeAll(fkSet);
		Iterator<String> iteratorFK = fkSet.iterator();
		while (iteratorFK.hasNext()) {
			// get colname
			String fkColName = iteratorFK.next();
			// get colIndex
			Integer fkIndex = Integer.parseInt(getColIndex(tableName, fkColName));
			// get value
			Column fkValue = (Column) values.get(fkIndex);
			// add to list the index would be updated after
			if (fkValue.isPk() || fkValue.isFk()) {
				tempPkList.add(fkIndex);			
			}
			colsBuff.append(fkColName);
			colsValues.append(fkValue.getValue());
			if (iteratorFK.hasNext()) {
				colsBuff.append(",");
				colsValues.append(",");
			}
		}
		if (colsBuff.length() > 0) {
			colsBuff.append(",");
		}
		if (colsValues.length() > 0) {
			colsValues.append(",");
		}
		// second iterate over common columns
		Set<String> colSet = getColsCache(tableName);
		Iterator<String> iteratorCol = colSet.iterator();
		while (iteratorCol.hasNext()) {
			final String colName = iteratorCol.next();
			// get colIndex
			Integer colIndex = Integer.parseInt(getColIndex(tableName, colName));
			// get value
			Column colValue = (Column) values.get(colIndex);
			if (pkSet != null && pkSet.contains(colName)) {
				// update pk in list to be updated after.
				tempPkList.add(colIndex);	
			}			
			colsBuff.append(colName);
			colsValues.append(colValue.getValue());
			if (iteratorCol.hasNext()) {
				colsBuff.append(",");
				colsValues.append(",");
			}
		}
		Map<String, String> mapCols = new HashMap<String, String>();
		mapCols.put("names", colsBuff.toString().trim().replaceAll(",$", ""));
		mapCols.put("values", colsValues.toString().trim().replaceAll(",$", ""));
		return mapCols;
	}
	/**
	 * @param tableName
	 * @param colName
	 * @return
	 */
	private String getColIndex(String tableName, final String colName) {
		final String key = tableName+" "+colName;
		String colIndex = (String) MemoryCache.INSTANCE.getFromMemory(key);
		if (colIndex == null) {
			colIndex = loader.getColValue(tableName, colName).toString();
			MemoryCache.INSTANCE.putMemory(key, colIndex);
		}
		return colIndex;
	}
	/**
	 * @param tableName
	 * @return
	 */
	private Set<String> getColsCache(String tableName) {
		final String key = tableName+"COL";
		Set<String> colSet = (Set<String>) MemoryCache.INSTANCE.getFromMemory(key);
		if (colSet == null) {
			colSet = (Set<String>) loader.loadCols(tableName);
			MemoryCache.INSTANCE.putMemory(key, colSet);
		}
		return colSet;
	}
	/**
	 * @param tableName
	 * @return
	 */
	private Set<String> getPKSet(String tableName) {
		final String key = tableName+"PK";
		Set<String> pkSet = (Set<String>) MemoryCache.INSTANCE.getFromMemory(key);
		if (pkSet == null) {			
			pkSet = (Set<String>) (Set<String>) loader.loadPKs(tableName);
			MemoryCache.INSTANCE.putMemory(key, pkSet);
		}
		return pkSet;
	}
	/**
	 * @param tableName
	 * @return
	 */
	private Set<String> getFKSet(String tableName) {
		String key = tableName+"FK";
		Set<String> fkSet = (Set<String>) MemoryCache.INSTANCE.getFromMemory(key);
		if (fkSet == null) {
			fkSet = (Set<String>) (Set<String>) loader.loadFKs(tableName);
			MemoryCache.INSTANCE.putMemory(key, fkSet);
		}
		return fkSet;
	}

	/**
	 * Increment Pk´s when allowed to do.
	 * @param fkValue
	 */
	private void updatePKValues() {
		for (Integer fkIndex : tempPkList) {
			Column fkValue = (Column) values.get(fkIndex);
			String value = (String) fkValue.getValue();
			String colValue = null;
			BigDecimal precision = fkValue.getPrecision();
			BigDecimal maximumAllowed = getMaximumNumberOf(precision);
			if (fkValue.getType().equals(Integer.class)) {
				Integer intValue = Integer.parseInt(value);				
				intValue++;
				BigDecimal newValue = new BigDecimal(intValue);
				if (newValue.compareTo(maximumAllowed) <= 0) {
					colValue = intValue.toString();	
					fkValue.setValue(colValue);
					values.set(fkIndex, fkValue);
				} else {
					System.out.println("Valor PK extrapolado nao insertar. ("+intValue+")");
				}				
			} else if (fkValue.getType().equals(Long.class)) {
				Long longValue = Long.parseLong(value);
				longValue++;
				BigDecimal newValue = new BigDecimal(longValue);
				if (newValue.compareTo(maximumAllowed) <= 0) {
					colValue = longValue.toString();	
					fkValue.setValue(colValue);
					values.set(fkIndex, fkValue);
				} else {
					System.out.println("Valor PK extrapolado nao insertar. ("+longValue+")");
				}
			} else {
				System.err.println("Tipo de datos PK no pudo ser agregado: "+fkValue.getType());
			}
			
		}		
	}
	
	/**
	 * Calculate if pk is not bigger than maximum allowed on column's precision.
	 * @param precision
	 * @return
	 */
	private BigDecimal getMaximumNumberOf(BigDecimal precision) {
		BigDecimal maximumNumber = null;
		int intValue = precision.intValue();
		StringBuilder concat = new StringBuilder();
		for (int i = 0; i < intValue; i++) {
			concat.append("9");
		}
		maximumNumber = new BigDecimal(concat.toString());
		return maximumNumber;
	}
	
	public void setDependencies(Set<String> dependencies) {
		this.dependencies = dependencies;
	}

	public void setRecordQuantity(Integer recordQuantity) {
		this.recordQuantity = recordQuantity;
	}

	public void setConnectionProps(ConnectionProperties connProps) {
		this.connProps = connProps;
	}
	
	
}
