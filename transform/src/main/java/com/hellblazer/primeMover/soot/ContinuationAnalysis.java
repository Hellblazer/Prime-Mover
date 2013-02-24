/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.soot;

import static com.hellblazer.primeMover.soot.util.Utils.markContinuable;
import static com.hellblazer.primeMover.soot.util.Utils.markTransformed;
import static com.hellblazer.primeMover.soot.util.Utils.willContinue;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

import com.hellblazer.primeMover.soot.util.IdentitySet;

/**
 * Performs the continuation analysis. Methods that call blocking or continuable
 * methods are marked as continuable. The process continues until a fixed point
 * in the analysis is reached.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class ContinuationAnalysis extends SceneTransformer {
    private static Logger log = Logger.getLogger(ContinuationAnalysis.class.getCanonicalName());

    @SuppressWarnings("unchecked")
	public ContinuationAnalysis() {
        Scene.v().setEntryPoints(Collections.EMPTY_LIST);
    }

    public void computeFixedPoint() {
        IdentityHashMap<SootMethod, IdentitySet<SootMethod>> callGraph = constructCallGraph();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (SootClass applicationClass : Scene.v().getApplicationClasses()) {
                for (SootMethod method : applicationClass.getMethods()) {
                    if (!willContinue(method)) {
                        IdentitySet<SootMethod> edges = callGraph.get(method);
                        if (edges != null) {
                            for (SootMethod edge : edges) {
                                if (willContinue(edge)) {
                                    markContinuable(method);
                                    markTransformed(method, this,
                                                    "Inferred continuation status");
                                    changed = true;
                                    log.info(String.format("Inferred continuable status of %1s.  Marking method as @Continuable",
                                                           method));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
	private void addEdges(SootMethod method,
                          IdentityHashMap<SootMethod, IdentitySet<SootMethod>> callGraph) {

        Iterator statements = method.retrieveActiveBody().getUnits().snapshotIterator();
        while (statements.hasNext()) {
            Stmt stmt = (Stmt) statements.next();
            if (stmt.containsInvokeExpr()) {
                IdentitySet<SootMethod> called = callGraph.get(method);
                if (called == null) {
                    called = new IdentitySet<SootMethod>();
                    callGraph.put(method, called);
                }
                called.add(stmt.getInvokeExpr().getMethod());
            }
        }
    }

    private IdentityHashMap<SootMethod, IdentitySet<SootMethod>> constructCallGraph() {
        IdentityHashMap<SootMethod, IdentitySet<SootMethod>> callGraph = new IdentityHashMap<SootMethod, IdentitySet<SootMethod>>();
        for (SootClass applicationClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : applicationClass.getMethods()) {
                if (method.isConcrete()) {
                    addEdges(method, callGraph);
                }
            }
        }
        return callGraph;
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected void internalTransform(String phaseName, Map options) {
        computeFixedPoint();
    }
}
