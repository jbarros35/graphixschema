package com.graphixschema.database.graph;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public enum DBForestProvider {

	/**
	 * Get an instance of provider
	 */
	INSTANCE;
	
	private Set<DBTree> listTree;
	
	private DBForestProvider() {
		listTree = new LinkedHashSet<DBTree>();
	}	
	/**
	 * Its only inserts a simple parent node.
	 * Find a parent and inserts on it.
	 * find the children and gets instead of create a new one.
	 * @param nodeName
	 * @param nodesName
	 */
	public void addNode(String nodeName, String parentName) throws Exception {
		try {
			NodeFinder finder = new NodeFinder();		
			finder.setListTree(listTree);
			// avoid insert same node again.
			if (finder.getNode(nodeName) != null) {
				return;
			}
			
			// Search the parent
			DBTree parent = null;
			if (parentName != null) {				
				parent = finder.getNode(parentName);
				// create the parent if needed
				/*if (parent == null) {					
					parent = new DBTree(getNodeId(), parentName, null);
					listTree.add(parent);
				}*/
			}
			// add to tree if its a new one.
			DBTree dbTree = new DBTree(getNodeId(), nodeName, parent);			
			if (parent != null) {
				parent.addChildNode(dbTree);
			}
			
			// only needed for high lvl tables
			if (parentName == null) {
				listTree.add(dbTree);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 
	 * @param nodeName node to insert
	 * @param parentName parent node
	 * @param duplicate if needed to check duplication
	 * @throws Exception
	 */
	public void addNode(String nodeName, String parentName, boolean duplicate) throws Exception {
		try {
			NodeFinder finder = new NodeFinder();		
			finder.setListTree(listTree);
			if (!duplicate) {				
				// avoid insert same node again.
				if (finder.getNode(nodeName) != null) {
					return;
				}
			}
			// Search the parent
			DBTree parent = null;
			if (parentName != null) {				
				parent = finder.getNode(parentName);
				// create the parent if needed
				if (parent == null) {
					//return;
					parent = new DBTree(getNodeId(), parentName, null);
					listTree.add(parent);
				}
			}
			// add to tree if its a new one.
			DBTree dbTree = new DBTree(getNodeId(), nodeName, parent);			
			if (parent != null) {
				parent.addChildNode(dbTree);
			}
			
			// only needed for high lvl tables
			if (parent == null) {
				listTree.add(dbTree);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void appendNode(String nodeName, String parentName) throws Exception {
		appendNode(nodeName, parentName, false);
	}
	
	public void appendNode(String nodeName, String parentName, boolean multiparent) throws Exception {
		try {
			NodeFinder finder = new NodeFinder();		
			finder.setListTree(listTree);
			
			// Search the parent
			DBTree parent = null;
			if (parentName != null) {				
				parent = finder.getNode(parentName);
				// create the parent if needed
				if (parent == null) {
					//return;
					parent = new DBTree(getNodeId(), parentName, null);
					listTree.add(parent);
				}
			}
			// add to tree if its a new one.
			DBTree dbTree = new DBTree(getNodeId(), nodeName, parent);		
			if (multiparent) {
				dbTree.setMultiparent(multiparent);
				parent.addChildNode(dbTree);	
			}
			
			if (parent != null) {
				parent.addChildNode(dbTree);
			}
			
			// only needed for high lvl tables
			if (parent == null) {
				listTree.add(dbTree);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Add a high lvl node without any parent
	 * @param nodeName
	 */
	public void addSuperNode(String nodeName) {
		DBTree parent = new DBTree(getNodeId(), nodeName, null);
		listTree.add(parent);
	}
	
	/**
	 * 
	 * @param nodeName
	 * @param parent
	 */
	public void addMultiParentNode(String nodeName, String parentName) {
		NodeFinder finder = new NodeFinder();	
		finder.setListTree(listTree);
		DBTree parent = finder.getNode(parentName);
		if (parent == null) {
			parent = new DBTree(getNodeId(), parentName, null);
			listTree.add(parent);
		}
		
		DBTree dbTree = new DBTree(getNodeId(), nodeName, parent);
		dbTree.setMultiparent(true);
		parent.addChildNode(dbTree);
	
	}
		
	/**
	 * 
	 * @param nodeName
	 * @param parentName
	 */
	public void addMultiParentNode(String nodeName, Set<String> parents) {
		NodeFinder finder = new NodeFinder();	
		finder.setListTree(listTree);
		for (String parentName : parents) {
			DBTree parent = finder.getNode(parentName);
			DBTree dbTree = finder.getNode(nodeName);
			if (parent != null) {
				if (dbTree == null) {
					dbTree = new DBTree(getNodeId(), nodeName, parent);
				}				
			} else {
				parent = new DBTree(getNodeId(), parentName, null);
				dbTree = new DBTree(getNodeId(), nodeName, parent);
			}			
			dbTree.setMultiparent(true);
			parent.addChildNode(dbTree);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private Long getNodeId() {
		return UUID.randomUUID().getMostSignificantBits();	
	}
	
	/**
	 * Empty the tree
	 */
	public void clear() {
		if (listTree != null) {
			listTree.clear();
		}
		listTree = new LinkedHashSet<DBTree>();
	}

	/**
	 * 
	 * @return
	 */
	public Set<DBTree> getListTree() {
	
		return listTree;
	}
	
	
}
