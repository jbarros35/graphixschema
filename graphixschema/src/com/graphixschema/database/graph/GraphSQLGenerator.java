package com.graphixschema.database.graph;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.graphixschema.database.vo.Column;




public class GraphSQLGenerator {
	

	List values;
	DBForestLoader loader;
	
	
	public GraphSQLGenerator(DBaseRuleLoader generator) {
		super();
		// create an instance of DBForestLoader to integrate with prolog
		this.loader = new DBForestLoader();
		loader.loadForest((File) generator.getFile());
		// load values
		this.values = generator.getColumns();
	}

	/**
	 * Iterate over treeMerged and return sql buffer.
	 * @return
	 */
	public String generateSQL(Set<String> dependencies) {		
		StringBuilder buffer = new StringBuilder();
		Iterator<String> itTree = dependencies.iterator();
		// iterate over nodes merged.
		while (itTree.hasNext()) {		
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
			buffer.append(");");
			buffer.append("\n");	
		}
		return buffer.toString();
	}

	/**
	 * Return a string concat with fk and cols names.
	 * @param tableName
	 */
	private Map<String, String> getCols(String tableName) {
		StringBuilder colsValues = new StringBuilder();
		StringBuilder colsBuff = new StringBuilder();
		// get first the fks set
		Set<String> fkSet = (Set<String>) loader.loadFKs(tableName);
		Iterator<String> iteratorFK = fkSet.iterator();
		while (iteratorFK.hasNext()) {
			// get colname
			String fkColName = iteratorFK.next();
			// get colIndex
			Integer fkIndex = Integer.parseInt(loader.getColValue(tableName, fkColName).toString());
			// get value
			Column fkValue = (Column) values.get(fkIndex);
			
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
		Set<String> colSet = (Set<String>) loader.loadCols(tableName);
		Iterator<String> iteratorCol = colSet.iterator();
		while (iteratorCol.hasNext()) {
			final String colName = iteratorCol.next();
			// get colIndex
			Integer colIndex = Integer.parseInt(loader.getColValue(tableName, colName).toString());
			// get value
			Column colValue = (Column) values.get(colIndex);
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
}
