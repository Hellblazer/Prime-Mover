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

import static com.hellblazer.primeMover.soot.util.Utils.markTransformed;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

/**
 * Transform calls on Kronos to invoke the simulation framework methods on
 * Kairos
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class ApiTransformer extends BodyTransformer {
    private SootClass kairos = Scene.v().forceResolve("com.hellblazer.primeMover.runtime.Kairos", SootClass.SIGNATURES);
    private SootClass kronos = Scene.v().forceResolve("com.hellblazer.primeMover.Kronos", SootClass.SIGNATURES);

    private final boolean validate;

    public ApiTransformer() {
        this(false);
    }

    public ApiTransformer(boolean validate) {
        this.validate = validate;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void internalTransform(Body body, String phaseName, Map options) {
        boolean transformed = false;
        PatchingChain<Unit> units = body.getUnits();
        for (Unit unit : units) {
            if (unit instanceof InvokeStmt) {
                InvokeStmt stmt = (InvokeStmt) unit;
                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                transformed |= mutateInvoke(invokeExpr);
            } else if (unit instanceof AssignStmt && ((Stmt) unit).containsInvokeExpr()) {
                InvokeExpr expr = ((AssignStmt) unit).getInvokeExpr();
                transformed |= mutateInvoke(expr);
            }
        }
        if (transformed) {
            markTransformed(body.getMethod(), this, "Transformed Kronos api calls");
            if (validate) {
                body.validate();
            }
        }
    }

    private boolean mutateInvoke(InvokeExpr invokeExpr) {
        if (invokeExpr.getMethodRef().getDeclaringClass().equals(kronos)) {
            SootMethod replacement = kairos.getMethod(invokeExpr.getMethod().getSubSignature());
            invokeExpr.setMethodRef(replacement.makeRef());
            return true;
        }
        return false;
    }

}
