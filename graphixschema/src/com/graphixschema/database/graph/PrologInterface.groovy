package com.graphixschema.database.graph

import java.util.regex.Pattern.Prolog;

import alice.tuprolog.SolveInfo
import alice.tuprolog.Term

class PrologInterface {

	/**
	 * Get a list from a query solution and var
	 * @param solution
	 * @param varname
	 * @return
	 */
	static def Set getList(SolveInfo solution, varname) {
		Set value = []
		if (solution.isSuccess) {
			String term = solution.getVarValue(varname)
			// avoid return same value
			if (term != null && !term.equals(varname)) {
				term = term.replaceAll(/(\[)/, '').replaceAll(/(\])/, '')	
				term.split(/,/).each {val->
					if (val != null && !val.isEmpty()) {
						value << val.replaceAll(/'/, '')
					}
				}
			}
		}
		value
	}
	/**
	 * 
	 * @param solution
	 * @param varname
	 * @return
	 */
	static def String getVar(SolveInfo solution, varname) {
		def value
		if (solution.isSuccess) {
			String term = solution.getVarValue(varname)
			if (term != null) {
				value = term
			}
		}
		value
	}
	
	/**
	 * 
	 * @param solution
	 * @param varname
	 * @param engine
	 * @return
	 */
	static def getAllSolutions(SolveInfo solution, varname, Prolog engine) {
		def Set solutions = []
		if (solution.isSuccess()) {
			solutions.add(solution.getTerm(varname).toString().replaceAll(/'/, ''));
			solution = engine.solveNext();
			while (engine.hasOpenAlternatives()) {
				solutions.add(solution.getTerm(varname).toString().replaceAll(/'/, ''));
				solution = engine.solveNext();
			}
		}
		solutions
	}
	
}
