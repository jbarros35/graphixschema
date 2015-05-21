package com.graphixschema.database.graph;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DirectedMultigraph;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxGraphControl;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;

/**
 * 
 * @author jlopesde
 * 
 */
public class GraphViewer extends JScrollPane {

	private static final int LEFT_CLICK = 4;
	private static final int RIGHT_CLICK = 16;
	private static final int SHIFT_CLICK = 17;
	JGraphXAdapter<String, DefaultEdge> jgxAdapter;
	private Map<String, Object> defaultStyle;
	private Set<mxCell> selectedVertex;
	private mxGraphComponent graphComponent;
	private File prologMap;
	// selected vertex.
	private final Set<mxCell> target = new LinkedHashSet<mxCell>();
	
	/**
	 * 
	 */
	public void viewGraph() {
		GraphLoader loader = new GraphLoader();
		loader.setMapFile(prologMap);
		loader.loadGraph();

		DirectedMultigraph graph = GraphProvider.INSTANCE.getGraph();
		jgxAdapter = new JGraphXAdapter<String, DefaultEdge>(graph);

		graphComponent = new mxGraphComponent(jgxAdapter);
		graphComponent.setPanning(true);
		// add graph to scroll pane
		getViewport().add(graphComponent);
		this.setVisible(true);

		jgxAdapter.setAllowDanglingEdges(false);
		jgxAdapter.setCellsDisconnectable(false);
		jgxAdapter.setEdgeLabelsMovable(false);
		
		defaultStyle = jgxAdapter.getStylesheet().getDefaultVertexStyle();

		final mxGraphControl control = graphComponent.getGraphControl();
		// add zoom functionality
		control.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					graphComponent.zoomIn();
				} else {
					graphComponent.zoomOut();
				}
			}

		});
		// control mouse events
		control.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			// select nodes and its parents.
			@Override
			public void mousePressed(MouseEvent e) {
				// avoid left click
				if (e.getModifiers() == RIGHT_CLICK) {
					// reset all changes to default
					resetStyle();
					mxCell selected = (mxCell) jgxAdapter.getSelectionCell();
					if (selected != null && selected.isVertex()) {
						target.add(selected);
						// call recursive method to change selection apearance
						selectNodes(selected);
						// change opacity of not related
						// get all vertices
						hideNoRelated();
					} 
					
					if (selected == null) {
						target.clear();
					}
					graphComponent.refresh();
				}
				
				if (e.getModifiers() == SHIFT_CLICK) {
					mxCell selected = (mxCell) jgxAdapter.getSelectionCell();
					if (selected != null && selected.isVertex()) {
						target.add(selected);
						// call recursive method to change selection apearance
						selectNodes(selected);
						// change opacity of not related
						// get all vertices
						hideNoRelated();
					} 
					
					if (selected == null) {
						//target.clear();
					}
					graphComponent.refresh();
				}
				// left click, show popup
				if (e.getModifiers() == LEFT_CLICK) {
					// get selected vertex
					mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(),e.getY());
					if (cell != null) {
						if (cell.isVertex()) {
							cell.setStyle("fillColor=white");
							graphComponent.refresh();
						}
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			// reset to default style
			@Override
			public void mouseClicked(MouseEvent e) {				
			}

		});
		// check if there is at least one edge, if not change layout
		Object[] cells = jgxAdapter.getChildVertices(jgxAdapter
				.getDefaultParent());
		Object[] edges = graphComponent.getGraph().getAllEdges(cells);
		
		setLayout(edges.length > 0);
		//graphComponent.setGridStyle(mxGraphComponent.GRID_STYLE_DASHED);
		graphComponent.setGridVisible(true);
		//graphComponent.setGridColor(Color.BLACK);
		graphComponent.setBackground(Color.WHITE);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
		
		jgxAdapter.setCellsMovable(false);
		jgxAdapter.setCellsEditable(false);
		jgxAdapter.setCellsResizable(false);
		selectedVertex = new LinkedHashSet<mxCell>();
		
	}

	/**
	 * 
	 */	
	private void setLayout(boolean hasEdges) {
		mxGraphLayout layout = null;
		if (hasEdges) {
			// define layout mxFastOrganicLayout
			layout = new mxFastOrganicLayout(jgxAdapter);
			((mxFastOrganicLayout) layout).setForceConstant(200);
		} else {
			layout = new mxOrganicLayout(jgxAdapter);
		}
		// layout using morphing
		jgxAdapter.getModel().beginUpdate();
		try {
			layout.execute(jgxAdapter.getDefaultParent());
		} finally {
			mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

			morph.addListener(mxEvent.DONE, new mxIEventListener() {

				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					jgxAdapter.getModel().endUpdate();
				}

			});

			morph.startAnimation();
		}
	}

	/**
	 * Select all related nodes.
	 * 
	 * @param vertex
	 */
	void selectNodes(mxCell vertex) {
		final String value = (String) vertex.getValue();

		jgxAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, "red",
				new Object[] { vertex });
		jgxAdapter.setCellStyles(mxConstants.STYLE_FONTCOLOR, "white",
				new Object[] { vertex });
		jgxAdapter.setCellStyles(mxConstants.STYLE_OPACITY, "100",
				new Object[] { vertex });
		jgxAdapter.setCellStyles(mxConstants.STYLE_TEXT_OPACITY, "100",
				new Object[] { vertex });
		// add to selection
		selectedVertex.add(vertex);
		int edges = vertex.getEdgeCount();
		for (int i = 0; i < edges; i++) {
			mxCell edge = (mxCell) vertex.getEdgeAt(i);
			mxCell parent = (mxCell) edge.getSource();
			if (!parent.getValue().equals(value)) {
				selectNodes(parent);
			}

		}

	}

	/**
	 * 
	 */
	private void hideNoRelated() {
		Object[] cells = jgxAdapter.getChildVertices(jgxAdapter
				.getDefaultParent());
		for (Object notSelected : cells) {
			if (!selectedVertex.contains(notSelected)) {
				mxCell cell = (mxCell) notSelected;
				cell.setStyle("opacity=50;textOpacity=50");
				int edges = cell.getEdgeCount();
				for (int i = 0; i < edges; i++) {
					mxCell edge = (mxCell) cell.getEdgeAt(i);
					edge.setStyle("opacity=50;textOpacity=50");
				}
			}
		}
	}

	/**
	 * 
	 */
	private void resetStyle() {
		Object[] cells = jgxAdapter.getChildVertices(jgxAdapter
				.getDefaultParent());
		for (Object cell : cells) {
			mxCell innCell = (mxCell) cell;
			innCell.setStyle("opacity=100");
			int edges = innCell.getEdgeCount();
			for (int i = 0; i < edges; i++) {
				mxCell edge = (mxCell) innCell.getEdgeAt(i);
				edge.setStyle("opacity=100;textOpacity=100");
			}
		}
		jgxAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
				(String) defaultStyle.get(mxConstants.STYLE_FILLCOLOR), cells);
		jgxAdapter.setCellStyles(mxConstants.STYLE_FONTCOLOR,
				(String) defaultStyle.get(mxConstants.STYLE_FONTCOLOR), cells);

		// clear the Set selection.
		selectedVertex.clear();
		selectedVertex = new LinkedHashSet<mxCell>();
		// clear target.
		target.clear();
	}

	/**
	 * 
	 * @param name
	 */
	public void searchCell(String name) {
		resetStyle();
		Object[] cells = jgxAdapter.getChildVertices(jgxAdapter
				.getDefaultParent());
		for (Object cell : cells) {
			mxCell innCell = (mxCell) cell;
			if (innCell.getValue().equals(name)) {
				target.add(innCell);
				graphComponent.scrollCellToVisible(innCell);
				selectNodes(innCell);
				hideNoRelated();
				break;
			}
		}

	}

	/**
	 * 
	 * @param vertex
	 */
	public Set<String> resolveDependencies() {
		Set<String> dependencies = new LinkedHashSet<String>();
		for (mxCell target : this.target) {
			final String vertex = (String) target.getValue();
			DependencyResolver resolver = new DependencyResolver();
			// recursion method for fill the map of dependencies.
			resolver.searchDependencies(vertex);
			// mount a List of dependencies in order
			dependencies.addAll(resolver.mountListDependencies(vertex));
		}
		return dependencies;
	}

	
	class DependencyResolver {
	
		// complete map of dependencies
		private Map<String, Set<String>> dependencyMap;
		// temporary list for high lvl nodes
		private Set<String> highLvlNodes;
		// store temporary list of subnodes.
		private List<String> dependentNodes;
			
		DependencyResolver() {
			dependencyMap = new HashMap();
			highLvlNodes = new LinkedHashSet<String>();
			dependentNodes = new ArrayList<String>();
		}
		
		
		/**
		 * Mount map of parents.
		 * @param vertex
		 */
		void searchDependencies(String vertex) {
			// set of parents
			Set<DirectionEdge> edgeSet = GraphProvider.INSTANCE
					.getParentEdges(vertex);
			// iterate over dependencies of vertex
			for (Iterator iterator = edgeSet.iterator(); iterator.hasNext();) {
				final DirectionEdge edge = (DirectionEdge) iterator.next();
				final String parent = (String) edge.getSource();
				addDependecy(vertex, parent);
				// call recursion
				searchDependencies(parent);
			}
		}
	
		/**
		 * Transform the map into a List of dependencies. 
		 */
		Set<String> mountListDependencies(String target) {
			// call method to separate the map into two lists
			fillLists(target);
			// merge lists into one.
			Set<String> mergedList = new LinkedHashSet<String>();
			// first insert the highLvl nodes
			mergedList.addAll(highLvlNodes);
			// reverse the dependentNodes
			Collections.reverse(dependentNodes);
			mergedList.addAll(dependentNodes);			
			// return the final List.
			return mergedList;
		}

		/**
		 * Fill highLvl and dependentNodes
		 * @param target
		 */
		private void fillLists(String target) {
			Set<String> dependencies = dependencyMap.get(target);
			// high lvl table, send to top of list.
			if (dependencies == null) {
				highLvlNodes.add(target);
			} else {
				dependentNodes.add(target);
				// if its has dependencies
				for (Iterator iterator = dependencies.iterator(); iterator
						.hasNext();) {
					String parent = (String) iterator.next();
					// call recursion to parent
					fillLists(parent);
				}
			}
		}

		/**
		 * 
		 * @param vertex
		 * @param parent
		 */
		private void addDependecy(String vertex, String parent) {
			Set<String> dependencies = dependencyMap.get(vertex);
			if (dependencies == null) {
				dependencies = new LinkedHashSet<String>();
			}
			dependencies.add(parent);
			dependencyMap.put(vertex, dependencies);
		}
		
	}
	

	public Set<mxCell> getTarget() {
		return target;
	}

	public File getPrologMap() {
		return prologMap;
	}

	public void setPrologMap(File prologMap) {
		this.prologMap = prologMap;
	}

	public static void main(String[] args) {
		GraphViewer viewer = new GraphViewer();
		viewer.setPrologMap(new File(
				"C:/temp/mapTeste.pl"));
		viewer.viewGraph();
		/*viewer.searchCell("S_SURTIDO_PROVEEDOR_PROD");
		Set<String> dependencies = viewer.resolveDependencies((String) viewer.getTarget().getValue());
		System.out.println(dependencies);*/
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(viewer);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);

	}
}
