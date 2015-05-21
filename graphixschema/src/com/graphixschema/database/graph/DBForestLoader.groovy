package com.graphixschema.database.graph

import java.util.Set;

import groovy.transform.CompileStatic;
import alice.tuprolog.Prolog
import alice.tuprolog.SolveInfo
import alice.tuprolog.Theory
import alice.tuprolog.TheoryManager

class DBForestLoader {

	static final String PROLOG_QUERY_FINDER_PL = "/prolog/QueryFinder.pl"

	def Prolog engine
	
	//def Theory forestMap
	
	def Set multiParentNodes = []
	
	def File mapFile
	
	// avoid unnecessary process in recursive methods.
	def Set<String> stack
	
	// set of all high lvl tables
	def Set<String> highLvlSet
	
	/**
	 * Load the prolog map
	 * @param file
	 */
	def loadForest(File file) {
		// load prolog map		
		engine = new Prolog()		
		// load database map
		def forestMap = new Theory(new FileInputStream(file))
		// load prolog custom predicates		
		def colTheory = new Theory(DBForestLoader.class.getResourceAsStream(DBForestLoader.PROLOG_QUERY_FINDER_PL))
		
		// append predicates to DBMap
		engine.setTheory(colTheory)
		def TheoryManager manager = engine.getTheoryManager()	
		manager.consult(forestMap, false, null)
		mapFile = file
	}
	
	/**
	 * Create an prolog engine in a separated thread.
	 * @return
	 */
	def getEngine() {
		def engine = new Prolog()
		// load database map
		def forestMap = new Theory(new FileInputStream(mapFile))
		// load prolog custom predicates
		def colTheory = new Theory(DBForestLoader.class.getResourceAsStream(DBForestLoader.PROLOG_QUERY_FINDER_PL))
		// append predicates to DBMap
		forestMap.append(colTheory)
		engine.setTheory(forestMap)
		engine
	}
			
	/**
	 * 
	 * @return
	 */
	def loadHighLvlTables() {
		// load all tables in a List
		def query = "findall(X, (is_table(X), not(is_fk(X,_,_,_))),List)."
		highLvlSet = PrologInterface.getList(engine.solve(query), "List")
		// add all high lvl nodes first.
		highLvlSet.each { highLvlTbl->
			DBForestProvider.INSTANCE.addSuperNode(highLvlTbl)			
		}
		// init stack
		stack = new LinkedHashSet<String>()
		// load simple child of all high lvl tables
		highLvlSet.each { highLvlTbl->
			loadSimpleChild(highLvlTbl, null, stack)
		}
		
	}
	
	/**
	 * It will start from a highlvl table and recursively until get the nodes inside.
	 * 
	 * @param table
	 * @return
	 */
	@CompileStatic
	def loadSimpleChild(String tableName, String parentName, Set<String> stack) {
		
		println parentName == null ? "+ $tableName" : "- $tableName" 
		
		// initialize the parent
		if (parentName == null) {
			parentName = tableName
		}
		//println "$stack"
		// check the parents of a table, if its simple add or else treat as multiparent.
		def Set<String> parents = getAllParents(tableName)
		// check if table is not multiparent
		if (parents.size() <= 1) {
			DBForestProvider.INSTANCE.addNode(tableName, parentName)
			def Set<String> children = (Set<String>) getChildOf(tableName)
			//println "child: $children"
			// get the subnodes
			children.each{String node->
				// check if nodeName is not empty
				if (!node.isEmpty()) {
					loadSimpleChild(node, tableName, stack)
				}
			}
			
		} else {
			//println "multiparent: $tableName"
			loadMultiParent(tableName, parentName)
		}
	}
	
	/**
	 *
	 * @param target table to found
	 * @param parents list of parents
	 * @return
	 */
	@CompileStatic
	def loadMultiParent(String tableName, String parentName) {
		println "M: $tableName < $parentName"
		// check the set of parents
		def Set<String> parents = getAllParents(tableName)
		// check if table is multiparent
		if (parents.size() > 1) {			
			// add child to parents
			DBForestProvider.INSTANCE.addMultiParentNode(tableName, parentName)
		} else {
			// add child to single parent
			DBForestProvider.INSTANCE.addNode(tableName, parentName)
		}
		// get the subnodes of parent
		getChildOf(parentName).each{String node->
			// check if nodeName is not empty and avoid call again for the same node.
			if (!node.isEmpty() && !node.equals(tableName)) {
				loadMultiParent(node, tableName)
			}
		}
		// get the subnodes of actual table and load it.
		getChildOf(tableName).each{String node->
			// check if nodeName is not empty and avoid call again for the same node.
			if (!node.isEmpty() && !node.equals(tableName)) {
				loadMultiParent(node, tableName)
			}
		}
	}
	
	/**
	 * 
	 * @param tableName
	 * @return
	 */
	def isHighLvlTable(tableName) {
		highLvlSet.contains(tableName)
	}
	
	/**
	 * Try to avoid sparse bug, it's necessary the parent load only child that its within 
	 * the bloodline of target table.
	 * The sparse bug is when we load unnecessary dependencies and recursively lost resources.
	 */
	@CompileStatic
	def bottomUpLoad(String targetTable) {
		if (isHighLvlTable(targetTable)) {
			return
		}
		
		// init stack
		stack = new LinkedHashSet<String>()
		def parentSet = new LinkedHashSet()
		// get the superParents of target
		loadSuperParents(targetTable, parentSet)
		//println parentSet
		parentSet.each {String parent->
			// add super node again the tree is empty
			DBForestProvider.INSTANCE.addSuperNode(parent)
			loadSuperTable(parent, targetTable)
		}
	}	
	
	@CompileStatic
	def loadSuperTable(String parent, String target) {
		// load all child of parent.
		getChildOf(parent).each {String child->
			
			if (!stack.contains(child)) {
				// check if table is related to target table at any level.
				def query = "bloodline('$target', '$child')."
				def bloodline = engine.solve(query).isSuccess()
				
				def Set parentSet = getAllParents(child) 
				
				//println "bloodline $bloodline $child"		
				if (parentSet.size() <= 1) {
					//DBForestProvider.INSTANCE.appendNode(child, parent)
				}				
				if (bloodline) {					
					// check parent of child					
					if (parentSet.size() > 1) {
						// call recursion for its own child.											
						// load all parents, even an multi-parent node always have at least one simple parent that is already loaded.						
						// loadMultiParent(child, target, parentSet)
						println "multiparent $child"
					} else {										
						loadSuperTable(child, target)
					}
				}
			}
			// when target is found.
			if (target.equals(child)) {
				//println "target found inside $parent"
				DBForestProvider.INSTANCE.addNode(target, parent, false)
				return
			}
			stack << child
		}
	}
	
	
		
	/**
	 * Load a desired table and its parents, check if they all are related directly
	 * avoid the dispersion.
	 * @param target
	 * @param parent
	 * @return
	 */
	def loadTarget(targetTable, parent) {
		if (targetTable.equals(parent)) {
			return
		}
		// iterate over child of parent.
		getChildOf(parent).each {child->
			// check if its already loaded.
			if (!stack.contains(child)) {
				// check if table is related to target table at any level.
				def query = "bloodline('$targetTable', '$child')."
				def bloodline = engine.solve(query).isSuccess
				// it target and child is related.
				if (bloodline) {
					// check the parents of a table, if its simple add or else treat as multiparent.
					def Set<String> parents = getAllParents(child)
					// check if table is not multiparent
					if (parents.size() <= 1) {
						// add to tree.
						DBForestProvider.INSTANCE.addNode(child, parent)
						//loadUntilFind(targetTable, parent)
					} else {
						println "multiparent table: $child"			
						//loadMultiParent(child, parents, stack)			
					}
					// recursion call.
					loadTarget(targetTable, child)
				}
				stack << child
			}
		}
		
	}
		
	/**
	 *
	 * @param targetName
	 * @param parentName
	 * @return
	 */
	def loadUntilFind(targetName, parentName) {
		println "loadUntil Parent = $parentName, target = $targetName"
		def Set<String> parents = getAllParents(parentName)
		// check if table is not multiparent
		if (parents.size() <= 1) {

			getChildOf(parentName).each {childOf->
				
				def query = "bloodline('$targetName', '$childOf')."
				def bloodline = engine.solve(query).isSuccess
				
				if (childOf.equals(targetName)) {
					DBForestProvider.INSTANCE.addNode(childOf, parentName)
					//println "table target $targetName found!"
					return
				} 
				if (bloodline) {
					// tail recursion
					loadUntilFind(targetName, childOf)
				}
			}
		} else {
			println "multiparent again: $parentName"
			//DBForestProvider.INSTANCE.addMultiParentNode(parentName, parents)
		}
	}
	
	/**
	 * @deprecated
	 * Called directly from table search.
	 * Load dependencies of nodes.
	 * @param table
	 * @return
	 */
	def loadDependencies(table, Set<String> setParent) {
		if (setParent == null) {
			setParent = new LinkedHashSet()
		}
		if (!setParent.contains(table)) {
			setParent << table
			if (isHighLvlTable(table)) {
				loadChild(table, null)
			} else {
				// get parents from table if its not highlvl
				def Set parents = getAllParents(table).each {parent->
					// load tree of parent.
					if (isHighLvlTable(parent)) {
						loadChild(table, null)
					} else {
						// tail recursion if its not high lvl table.
						loadDependencies(parent, setParent)
					}
				}
			}
		}	
		
	}
	/**
	 * Search all high lvl tables from a table.
	 * @param table target table to inspect
	 * @param parentSet collection of high lvl tables found.
	 * @return
	 */
	@CompileStatic
	def loadSuperParents(table, Set parentSet) {		
		// add to set if its high lvl
		if (isHighLvlTable(table)) {
			parentSet << table
		} else {
			// call recursively until find the high lvl table
			getAllParents(table).each {parent->
				loadSuperParents(parent, parentSet)
			}
		}
		
	}
			
	/**
	 * Set of subnodes of a table.
	 * @param table tablename
	 * @return
	 */
	def getChildOf(table) {
		def query = "tree('$table',X)."
		// get the subnodes into a set
		PrologInterface.getList(engine.solve(query), "X")
	}

	/**
	 * Check if a child and its parent makes an cycle reference, avoid stackoverflow.
	 * @param table
	 * @param parent
	 * @return
	 */
	def isCircularRef(table, parent) {
		def query = "get_parents('$table','$parent'), get_parents('$parent','$table')."
		engine.solve(query).isSuccess
	}
	
	
	
	/**
	 * Check if table name is valid
	 * @param tableName
	 * @return
	 */
	def tableExists(tableName) {
		def query = "is_table('$tableName')."		
		engine.solve(query).isSuccess
	}
	
	/**
	 * Return all FK cols.
	 * @param table
	 * @return
	 */
	def loadFKs(table) {
		// load the prolog file with query
		Theory colTheory = new Theory(DBForestLoader.class.getResourceAsStream(PROLOG_QUERY_FINDER_PL))
		engine.addTheory(colTheory)
		// set the query
		def query = "get_fks('$table', L)."
		Set cols = PrologInterface.getList(engine.solve(query), "L")
	}
	
	/**
	 * Return all pks into table.
	 * PK can be mixed with FK if table has multiple key, try to remove from above fkList.
	 * @param table
	 * @return
	 */
	@CompileStatic
	def Set<String> loadPKs(table) {
		def query = "pk('$table',X)."
		// get the subnodes into a set
		PrologInterface.getList(engine.solve(query), "X")
	}

	/**
	 * Return all no fk cols.
	 * @param table
	 * @return
	 */
	def loadCols(table) {
		// load the prolog file with query
		Theory colTheory = new Theory(DBForestLoader.class.getResourceAsStream(PROLOG_QUERY_FINDER_PL))
		engine.addTheory(colTheory)
		// set the query
		def query = "get_cols('$table', L)."
		Set cols = PrologInterface.getList(engine.solve(query), "L")
	}
	
	/**
	 * Return the index of array of values for a column.
	 * @param table
	 * @param col
	 * @return
	 */
	def getColValue(table, col) {
		Theory colTheory = new Theory(DBForestLoader.class.getResourceAsStream(PROLOG_QUERY_FINDER_PL))
		engine.addTheory(colTheory)
		def query = "col_value('$table','$col', V)."
		PrologInterface.getVar(engine.solve(query), "V")
	}
	
	/**
	 * Return all parents of table, at first lvl.
	 * @param table
	 * @return
	 */
	 @CompileStatic
	def Set<String> getAllParents(table) {
		def Set solutions = []
		// create an engine on a separated thread, avoid freeze bug, its method is called recursively.
		def Prolog engine = (Prolog) getEngine()
		// predicate to get parent of a table
		def query = "get_parents('$table', X)."		
		SolveInfo solution = engine.solve(query);
		if (solution.isSuccess()) {
			solutions.add(solution.getTerm("X").toString().replaceAll(/'/, ''));
			solution = engine.solveNext();
			while (engine.hasOpenAlternatives()) {
				solutions.add(solution.getTerm("X").toString().replaceAll(/'/, ''));
				solution = engine.solveNext();
			}
		}
		solutions		
	}
		
}
