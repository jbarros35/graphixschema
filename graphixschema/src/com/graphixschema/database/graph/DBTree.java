package com.graphixschema.database.graph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A tree node of tables.
 * @author jlopesde
 *
 */
public class DBTree implements Cloneable, Comparable {

	private String name;
	private Set<DBTree> nodes = new LinkedHashSet<DBTree>();
	private DBTree parent;
	// id to properly identify nodes with multiple parents.
	private Long id;
	private Integer depth = 0;
	
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * 
     */
	private boolean multiparent;

	
	public DBTree(Long id, String name, DBTree parent) {
		super();
		this.name = name;
		this.parent = parent;
		this.id = id;
	}
	
	/**
	 * 
	 * @param nodeName
	 * @param child
	 */
	public void addChildNode(DBTree child) {
		if (nodes == null) {
			nodes = new LinkedHashSet<DBTree>();
		}
		for (DBTree childNode : nodes) {
			if (childNode.getName().equals(child.getName())) {
				return;
			}
		}
		child.setParent(this);
		child.setMultiparent(isMultiparent());
		nodes.add(child);
	}
	
	public void addPropertyChangeListener(String propertyName,PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
   }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
       propertyChangeSupport.removePropertyChangeListener(listener);
   }
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<DBTree> getNodes() {
		return nodes;
	}
	
	public void setNodes(Set<DBTree> nodes) {
		this.nodes = nodes;
	}
	
	public DBTree getParent() {
		return parent;
	}
	/**
	 * If the father is multiparent the child will be too.
	 * @param parent
	 */
	public void setParent(DBTree parent) {
		this.parent = parent;		
	}
	
	public String toString() {
		return name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBTree other = (DBTree) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
	/**
	 * 
	 */
	public DBTree clone() throws CloneNotSupportedException {
		DBTree dbTree = new DBTree(null, name, null);
		return dbTree;
		
	}
	
	public boolean isMultiparent() {
		return multiparent;
	}

	/**
	 * Set all to multiparent state.
	 * @param multiparent
	 */
	public void setMultiparent(boolean multiparent) {
		this.multiparent = multiparent;				
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	@Override
	public int compareTo(Object o2) {
		DBTree node2 = (DBTree) o2;
		if (depth == node2.getDepth()) {
			return name.compareTo(node2.getName());
		}
		return depth.compareTo(node2.getDepth());
	}
}
