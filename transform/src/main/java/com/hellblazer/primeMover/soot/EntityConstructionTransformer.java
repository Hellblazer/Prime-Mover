/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.soot;

import static com.hellblazer.primeMover.soot.EntityGenerator.GENERATED_ENTITY_SUFFIX;
import static com.hellblazer.primeMover.soot.util.Utils.isEntity;
import static com.hellblazer.primeMover.soot.util.Utils.markTransformed;

import java.util.Map;
import java.util.logging.Logger;

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
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;

/**
 * Transform the creation of an entity to the creation of the proxy
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class EntityConstructionTransformer extends BodyTransformer {

    private static final String INIT = "<init>";
    private static Logger log = Logger.getLogger(EntityConstructionTransformer.class.getCanonicalName());

    private final boolean validate;

    public EntityConstructionTransformer() {
        this(false);
    }

    public EntityConstructionTransformer(boolean validate) {
        this.validate = validate;
    }

    private boolean transform(NewExpr newExpr, Unit initUnit,
                              PatchingChain<Unit> units) throws VerifyError {
        SootClass baseClass = newExpr.getBaseType().getSootClass();
        if (isEntity(baseClass)) {
            SootClass generatedClass = Scene.v().forceResolve(baseClass.getName().concat(GENERATED_ENTITY_SUFFIX),
                                                              SootClass.SIGNATURES);
            newExpr.setBaseType(generatedClass.getType());
            Stmt stmt = (Stmt) initUnit;
            while (stmt != null) {
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if (invokeExpr instanceof SpecialInvokeExpr) {
                        if (INIT.equals(invokeExpr.getMethod().getName())) {
                            SootMethod method = invokeExpr.getMethod();
                            if (method.getDeclaringClass().equals(baseClass)) {
                                invokeExpr.setMethodRef(generatedClass.getMethod(method.getName(),
                                                                                 method.getParameterTypes()).makeRef());
                                return true;
                            }
                        }
                    }
                }
                stmt = (Stmt) units.getSuccOf(stmt);
            }
            String errorMessage = String.format("Expected constructor invocation, found: %s",
                                                initUnit);
            log.severe(errorMessage);
            throw new VerifyError(errorMessage);
        }
        return false;
    }

    @Override
    protected void internalTransform(Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
        boolean transformed = false;
        PatchingChain<Unit> units = body.getUnits();
        for (Unit unit : units) {
            if (unit instanceof InvokeStmt) {
                InvokeExpr invoke = ((InvokeStmt) unit).getInvokeExpr();
                if (invoke instanceof NewExpr) {
                    transformed |= transform((NewExpr) invoke,
                                             units.getSuccOf(unit), units);
                }
            } else if (unit instanceof AssignStmt
                       && ((AssignStmt) unit).getRightOp() instanceof NewExpr) {
                NewExpr invoke = (NewExpr) ((AssignStmt) unit).getRightOp();
                transformed |= transform(invoke, units.getSuccOf(unit), units);
            }
        }
        if (transformed) {
            markTransformed(body.getMethod(), this,
                            "Transformed entity construction");
            if (validate) {
                body.validate();
            }
        }
    }
}
