package com.graphixschema.database.graph

import java.nio.channels.FileChannel;




import org.eclipse.swt.widgets.Tree;
import groovy.sql.GroovyResultSet;
import groovy.sql.Sql
import groovy.time.TimeCategory;
import groovy.transform.CompileStatic;
import groovy.util.logging.Log;

class DBaseRuleLoader {

	static def driver = "oracle.jdbc.driver.OracleDriver"
	
	// sql connection object
	def Sql sql
	// file object to prolog map
	def File file
	// out put stream
	def FileWriter fos
	
	
	// increment of array of values
	def Long increment = 1
	
	// default value for auto relationships.
	static final int NULLVALUE = 0
	
	/**
	 * List of all columns in order. SESSION OBJECT.
	 */
	def List columns = []

	//def ln = System.getProperty('line.separator')
	def ln = '\n'

	/**
	 * 
	 * @param url
	 * @param user
	 * @param pwd
	 */
	public void connect(url, user, pwd) {
		sql = Sql.newInstance(url, user,
			pwd, driver)
	}	
		
	/**
	 * Get all children of a table.
	 */
	@CompileStatic
	def Set<String> getDependentsTable(String parentTable) {
		def Set tableList = []
		def String query = "SELECT UC.TABLE_NAME "+
		"FROM user_constraints uc, user_cons_columns UCC1, user_cons_columns UCC2, user_tab_columns UCTAB "+  
		"WHERE uc.constraint_name = ucc1.constraint_name "+
		"AND uc.r_constraint_name = ucc2.constraint_name "+
		"AND ucc1.POSITION = ucc2.POSITION "+
		"AND UC.TABLE_NAME = UCTAB.TABLE_NAME "+
		"AND UCTAB.COLUMN_NAME = UCC1.COLUMN_NAME "+
		"AND uc.constraint_type = 'R' "+
		"AND UCTAB.NULLABLE = 'N' "+
		"AND UCC2.TABLE_NAME = '$parentTable' "+ 
		"AND UC.TABLE_NAME <> '$parentTable' "+
		"GROUP BY UC.TABLE_NAME"

		sql.eachRow(query) {GroovyResultSet childTable ->
			tableList.add(childTable.getAt("TABLE_NAME"))				
		}
		tableList
	}
	
	@CompileStatic
	def Set<String> getPK(String tableName) {
		def Set pkList = []
		def String query = "SELECT cols.table_name, cols.column_name, cols.position, cons.status, cons.owner "+ 
		"FROM user_constraints cons, all_cons_columns cols "+
		"WHERE cols.table_name = '$tableName' "+
		"AND cons.constraint_type = 'P' "+
		"AND cons.constraint_name = cols.constraint_name "+
		"AND cons.owner = cols.owner "+
		"ORDER BY cols.table_name, cols.position"
		sql.eachRow(query) {GroovyResultSet childTable ->
			pkList.add(childTable.getAt("column_name"))
		}
		pkList
	}
	
	
	/**
	 * Init method for create the file and the tree for all tables.
	 * Inside of it calls the method to create the column and fk predicates.
	 * @return
	 */
	@CompileStatic
	def generatePrologTree() {
		// create a temporary file
		init();
		// iterate over all tables
		sql.eachRow("SELECT table_name FROM user_tables order by table_name") {GroovyResultSet row ->
			String tableParent = row.getAt("TABLE_NAME")
			appendFile("% TABLE $tableParent $ln")
			appendFile("is_table('$tableParent'). $ln")
			// load children tree
			def Set<String> children = getDependentsTable(tableParent)
			if (children != null) {
				appendFile("tree('$tableParent',[")				
				StringBuilder buff = new StringBuilder()
				children.eachWithIndex{String tableChild, int index ->
					if (index < (children.size() - 1) && children.size() > 1) {
						buff.append("'")
						buff.append(tableChild)
						buff.append("',")
					} else {
						buff.append("'")
						buff.append(tableChild)
						buff.append("'")
					}
				}
				buff.append("]).$ln")
				appendFile(buff.toString())				
			}
			def Set<String> pkList = getPK(tableParent);
			if (pkList != null) {				
				StringBuilder buff = new StringBuilder()
				buff.append("pk('$tableParent',[")
				pkList.eachWithIndex{String pkName, int index ->
					if (index < (pkList.size() - 1) && pkList.size() > 1) {
						buff.append("'")
						buff.append(pkName)
						buff.append("',")					
					} else {
						buff.append("'")
						buff.append(pkName)
						buff.append("'")
					}
				}
				buff.append("]).$ln")
				appendFile(buff.toString())
			}
			// load column values and fks
			generatePrologMap(tableParent)
		}
				
		println "rows size $columns.size / $file.absolutePath"
		closeWriter()
	}

	/**
	 * Create the temp file
	 * @return
	 */
	def protected init() {
		file = File.createTempFile("DBaseMap", ".pl")
		fos = new FileWriter(file)
		println "$file.absolutePath"

		//file << "% TABLES TREE MAP $ln"
		appendFile("% TABLES TREE MAP $ln")
		// initiate with null object at position zero.
		GenerateValue gen = new GenerateValue()
		columns << gen.createColumn("null", "null", 0, true, null)
		file
	}
	
	@CompileStatic
	def appendFile(String content) {
		fos.append(content)
	}
	
	/**
	 * Generate prolog map of a table.
	 * @return
	 */
	@CompileStatic
	def generatePrologMap(String tableName) {
		
		GenerateValue gen = new GenerateValue()		
		// query obtain all columns and constraints inside it.						
		def String query = "SELECT DISTINCT COLUMN_NAME, FKCOLUMN_NAME, PKTABLE_NAME, "+
		" PKCOLUMN_NAME, data_type, data_precision, data_scale, nullable, data_length "+
		"FROM ( "+
	    " SELECT UCC1.COLUMN_NAME AS FKCOLUMN_NAME, UCC2.TABLE_NAME AS PKTABLE_NAME,"+ 
	    " UCC2.COLUMN_NAME AS PKCOLUMN_NAME, UCTAB.column_name, UCTAB.data_type, UCTAB.data_precision, data_scale, UCTAB.nullable, UCTAB.data_length "+
	    " FROM user_constraints uc,  user_cons_columns UCC1 ,  user_cons_columns UCC2, user_tab_columns UCTAB  "+
	    " WHERE uc.constraint_name = ucc1.constraint_name "+
	    " AND UC.TABLE_NAME = UCTAB.TABLE_NAME "+
	    " AND UCTAB.COLUMN_NAME = UCC1.COLUMN_NAME "+
	    " AND uc.r_constraint_name = ucc2.constraint_name "+ 
	    " AND ucc1.POSITION = ucc2.POSITION "+
	    " AND uc.constraint_type = 'R' "+
	    " AND UC.TABLE_NAME = '$tableName' "+ 
    	" UNION "+
    	" SELECT NULL AS FKCOLUMN_NAME, NULL AS PKTABLE_NAME, NULL AS PKCOLUMN_NAME, "+
    	" UCTAB.column_name, UCTAB.data_type, UCTAB.data_precision, data_scale, UCTAB.nullable, UCTAB.data_length "+
    	" FROM USER_TAB_COLUMNS UCTAB WHERE UCTAB.TABLE_NAME = '$tableName' "+
		" ) COLUMNS "
		def Set colSet = []
		sql.eachRow(query) {GroovyResultSet rs ->
			def colName = rs.getAt("COLUMN_NAME")
			// avoid treat same column twice
			if (!colSet.contains(colName)) {
				//  GENERATE PROLOG MAP For table and its columns
				boolean nullAble = (Boolean) GenerateValue.nullMap.get((String) rs.getAt("nullable"))
				
				// check if col is a FK
				if (rs.getAt("FKCOLUMN_NAME") != null) {
					// check if its an auto relation if it is put NULL for value.
					if (!tableName.equals(rs.getAt("PKTABLE_NAME")) && !nullAble) {
						String pkTable = rs.getAt("PKTABLE_NAME")
						String pkColumn = rs.getAt("PKCOLUMN_NAME")
						fos.append("is_fk('$tableName', '$colName', '$pkTable', '$pkColumn').$ln")
					} else {
						fos.append("is_column('$tableName', '$colName', $NULLVALUE).$ln")
					}
									
				} else {
					// treat the simple column
					fos.append("is_column('$tableName', '$colName', $increment).$ln")
				}	
				
				String type = rs.getAt("data_type")
				BigDecimal precision = (BigDecimal) rs.getAt("data_precision")
				
				int scale = 0;

				if (precision != null) {
					scale = (Integer) rs.getAt("data_scale")
					if (scale > 0) {
						precision = precision.setScale(scale);
					}
				} else {
					precision = (BigDecimal) rs.getAt("data_length")
				}

				gen.setRange(precision)
				// append column to list
				columns << gen.createColumn(colName, type, scale, nullAble, precision)
				colSet << colName
				increment++
			}
		}			
		
	}
	
	def closeWriter() {
		fos.close()
	}

	/**
	 * Do a copy of file from temp directory to a selected one.
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	def protected static copyFileUsingFileChannels(File source, File dest)
	throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			if (inputChannel != null) {
				inputChannel.close();
			}
			if (outputChannel != null) {
				outputChannel.close();
			}
		}
	}
	/**
	 * Save a serialized session and a prolog map.
	 */
	def void saveSession(String fileName) throws Exception {
		def dest = new File("$fileName")
		if (!dest.exists()) {			
			dest.createNewFile()
		}
		// COPY the prolog map to a new path		
		copyFileUsingFileChannels(file, dest)
		def serialFile = fileName.replace('.pl', '.ser')
		// Generate a serialized array
		def file = new File(serialFile)
		file.createNewFile()
		// save the object to file
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
		  fos = new FileOutputStream(file, true)
		  out = new ObjectOutputStream(fos)
		  out.writeObject(columns)	
		  if (out != null) {
			  out.close();
		  }
		} catch (Exception ex) {
		  throw ex
		}		
	}
	
	/**
	 * Load a serialized session.
	 */
	def void loadSession(file) throws Exception {
		FileInputStream fis = null;
		ObjectInputStream input = null;
		try {
			fis = new FileInputStream(file);
			input = new ObjectInputStream(fis);
			columns = input.readObject()			
			input.close()
		  } catch (Exception ex) {
			throw ex
		  }
	}
	
	/**
	 * Final method to generate the SQL scripts.
	 * @param tree
	 * @return
	
	def generateSQL(Tree tree) {
		// create an instance of DBForestLoader to integrate with prolog
		DBForestLoader loader = new DBForestLoader()
		loader.loadForest(file)
		// create an instance of treeSQLGenerator and merge trees.
		TreeSQLGenerator treeSQL = new TreeSQLGenerator(loader, columns)		
		treeSQL.mergeTrees(tree)
		// get the final buffer of SQL
		treeSQL.generateSQL()
	}
	 */
	/**
	 * Check if table exists.
	 * @param tableName
	 * @return
	 */
	def tableExists(tableName) {
		def int count = 0
		sql.eachRow("SELECT count(*) as count FROM user_tables where table_name = $tableName order by table_name") {
			count = it.count
		}
		count > 0
	}

	static def main (args) {
		Date init = new Date()
		println init
		DBaseRuleLoader loader = new DBaseRuleLoader()
		loader.connect("jdbc:oracle:thin:@dbserver:1521:xe", "TEMUCO_PLUGIN", "TEMUCO_PLUGIN")
		loader.generatePrologTree()
		Date finish = new Date()
		def duration = TimeCategory.minus(finish, init)
		println finish
		println "${duration.minutes}m : ${duration.seconds}s"
	}
	
}
