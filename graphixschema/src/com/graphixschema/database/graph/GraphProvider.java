package com.graphixschema.database.graph;

import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

public enum GraphProvider {

	INSTANCE;

	DirectedMultigraph<String, DirectionEdge> graph;
	
	private GraphProvider() {
		graph =
	            new DirectedMultigraph<String, DirectionEdge>(
	                    new ClassBasedEdgeFactory<String, DirectionEdge>(DirectionEdge.class));
	}

	/**
	 * 
	 * @param vertexName
	 */
	public void addVertex(String vertexName) {
		if (!graph.containsVertex(vertexName)) {
			graph.addVertex(vertexName);
		}
	}
	
	public DirectedMultigraph getGraph() {
		return graph;
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 */
	public void addEdge(String source, String target) {
		if (!graph.containsEdge(source, target)) {
			graph.addEdge(source, target, new DirectionEdge(source, target, source));
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<String> getVertexSet() {
		return graph.vertexSet();
	}
	
	/**
	 * return all incoming edges.
	 * @param vertex
	 * @return
	 */
	public Set<DirectionEdge> getParentEdges(String vertex) {
		return graph.incomingEdgesOf(vertex);
	}
	
	/**
	 * 
	 * @param vertex
	 * @return
	 */
	public Set<DirectionEdge> getChildEdges(String vertex) {
		return graph.outgoingEdgesOf(vertex);
	}
}
