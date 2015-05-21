package com.graphixschema.database.graph;

import org.jgrapht.graph.DefaultEdge;

public class DirectionEdge<V> extends DefaultEdge {
	private V source;
	private V target;
	private String direction;

	public DirectionEdge(V source, V target, String direction) {
		this.source = source;
		this.target = target;
		this.direction = direction;
	}

	public V getSource() {
		return source;
	}

	public void setSource(V source) {
		this.source = source;
	}

	public V getTarget() {
		return target;
	}

	public void setTarget(V target) {
		this.target = target;
	}

	public String getDirection() {
		return direction;
	}

	public String toString() {
		return direction;
	}
}
