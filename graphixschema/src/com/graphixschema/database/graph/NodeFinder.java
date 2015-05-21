package com.graphixschema.database.graph;

import java.util.LinkedHashSet;
import java.util.Set;

public class NodeFinder {

	private DBTree nodeFind = null;
	private Set<DBTree> listTree;
	
	/**
	 * Find a node on the tree.
	 * @param parentName
	 * @return
	 */
	private void findNode(String nodeName, Set<DBTree> list) {		
		// object to return recursively
		if (nodeName == null) {
			return;
		}
		if (list != null) {
			for (DBTree tree : list) {
				if (tree.getName().equals(nodeName)) {
					nodeFind = tree;
					break;
				} else {
					findNode(nodeName, tree.getNodes());
				}
			}
		}
	}
				
	public Set<DBTree> getListTree() {
		return listTree;
	}

	public void setListTree(Set<DBTree> listTree) {
		this.listTree = listTree;
	}

	public DBTree getNode(String nodeName) {
		if (listTree == null) {
			throw new IllegalArgumentException("listTree is null! use setListTree method.");
		}
		// clean old searches
		nodeFind = null;
		// search for node
		findNode(nodeName, listTree);
		// return node
		return nodeFind;
	}
	
}
