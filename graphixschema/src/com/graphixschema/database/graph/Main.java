package com.graphixschema.database.graph;

/**
 * Main class for execution.
 * @author jlopesde
 * @since 22-10-2014
 *
 */
public class Main {

	public static void main(String[] args) {
		GraphApp app = new GraphApp();
		app.setBlockOnOpen(true);
		app.open();
	}
}
