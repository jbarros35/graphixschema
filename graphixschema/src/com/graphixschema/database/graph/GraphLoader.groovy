package com.graphixschema.database.graph;

import groovy.transform.CompileStatic

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

import javax.swing.JFrame
import javax.swing.JScrollPane

import org.jgraph.JGraph
import org.jgrapht.ext.JGraphModelAdapter
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultEdge

import alice.tuprolog.Prolog
import alice.tuprolog.Theory
import alice.tuprolog.TheoryManager

import com.jgraph.layout.JGraphFacade
import com.jgraph.layout.JGraphLayout
import com.jgraph.layout.organic.JGraphFastOrganicLayout
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.util.mxConstants
import com.mxgraph.model.mxCell
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.swing.util.mxMorphing
import com.mxgraph.model.mxCell
import com.mxgraph.util.mxEvent
import com.mxgraph.util.mxEventObject
import com.mxgraph.util.mxEventSource.mxIEventListener

public class GraphLoader {

	static final String PROLOG_QUERY_FINDER_PL = "/prolog/QueryFinder.pl"

	def Prolog engine
	def File mapFile

	def initEngine() {
		// load prolog map
		engine = new Prolog()
		// load database map
		def forestMap = new Theory(new FileInputStream(mapFile))
		// load prolog custom predicates
		def colTheory = new Theory(GraphLoader.class.getResourceAsStream(GraphLoader.PROLOG_QUERY_FINDER_PL))

		// append predicates to DBMap
		engine.setTheory(colTheory)
		def TheoryManager manager = engine.getTheoryManager()
		manager.consult(forestMap, false, null)
	}

	@CompileStatic
	def loadGraph() {
		// init prolog engine
		initEngine()
		// get all high lvl tables
		getHighLvlTables().each {String superTable->
			//println "H: $superTable"
			// ADD to graph as vertex
			GraphProvider.INSTANCE.addVertex(superTable)
			// call recursive method for load child
			loadChildTable(superTable)
		}
	}

	@CompileStatic
	def loadChildTable(String parent) {

		getChildOf(parent).each {String child ->
			//println "C: $child"
			// add child
			GraphProvider.INSTANCE.addVertex(child)
			// add edge
			GraphProvider.INSTANCE.addEdge(parent, child)
			if (getChildOf(child).size() > 0) {
				// call recursion
				loadChildTable(child)
			}
		}
	}

	/**
	 * Set of subnodes of a table.
	 * @param table tablename
	 * @return
	 */
	@CompileStatic
	def Set<String> getChildOf(table) {
		def query = "tree('$table',X)."
		// get the subnodes into a set
		PrologInterface.getList(engine.solve(query), "X")
	}
	

	def debug() {
		Iterator<String> vertexSet = GraphProvider.INSTANCE.getVertexSet().iterator();
		println "Parent edges"
		while (vertexSet.hasNext()) {
			String vertex = vertexSet.next();
			System.out.println("V:"+vertex);
			Iterator<DirectionEdge> it = GraphProvider.INSTANCE.getParentEdges(vertex).iterator();
			System.out.print("Edges:[");
			while (it.hasNext()) {
				DirectionEdge edge = it.next();
				System.out.print(edge.source);
				System.out.print("->");
				System.out.print(edge.target);
				if (it.hasNext()) {
					System.out.print(",");
				}
			}
			System.out.println("]");
		}

		vertexSet = GraphProvider.INSTANCE.getVertexSet().iterator();
		println "Child edges:"
		while (vertexSet.hasNext()) {
			String vertex = vertexSet.next();
			System.out.println("V:"+vertex);
			Iterator<DirectionEdge> it = GraphProvider.INSTANCE.getChildEdges(vertex).iterator();
			System.out.print("Edges:[");
			while (it.hasNext()) {
				DirectionEdge edge = it.next();
				System.out.print(edge.source);
				System.out.print("->");
				System.out.print(edge.target);
				if (it.hasNext()) {
					System.out.print(",");
				}
			}
			System.out.println("]");
		}
	}

	/**
	 * 
	 * @return
	 */
	def displayGraph() {
		def graph = GraphProvider.INSTANCE.getGraph()
		// create the view, then add data to the model
		JGraphModelAdapter adapter = new JGraphModelAdapter(graph);
		JGraph jgraph = new JGraph(adapter);
		// set layout
		final JGraphLayout hir = new JGraphFastOrganicLayout();
		final JGraphFacade graphFacade = new JGraphFacade(jgraph);
		hir.run(graphFacade);

		final Map nestedMap = graphFacade.createNestedMap(true, true);
		// configure jgraph
		jgraph.getGraphLayoutCache().edit(nestedMap);
		jgraph.setDisconnectable(false)
		jgraph.setAutoResizeGraph(true)
		jgraph.setEdgeLabelsMovable(false)

		// set frame display
		JScrollPane scroller = new JScrollPane(jgraph);
		JFrame frame = new JFrame("The Body");
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setSize(600,600);
		frame.add(scroller);
		frame.setVisible(true);
	}

	/**
	 * 
	 * @author jlopesde
	 *
	 */
	@CompileStatic
	def displaymxGraph() {
		
	}

		/**
		 *
		 * @return
		 */
		def getHighLvlTables() {
			// load all tables in a List
			def query = "findall(X, (is_table(X), not(is_fk(X,_,_,_))),List)."
			PrologInterface.getList(engine.solve(query), "List")
		}

		static main(args) {
			def loader = new GraphLoader()
			loader.setMapFile(new File("C:/Users/jlopesde/Documents/Peticiones/plugin/luiz.pl"))
			loader.loadGraph()
			loader.displaymxGraph()
		}

	}
