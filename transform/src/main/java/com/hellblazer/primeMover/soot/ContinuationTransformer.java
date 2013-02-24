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
import static com.hellblazer.primeMover.soot.util.Utils.willContinue;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.UnusedLocalEliminator;

import com.hellblazer.primeMover.soot.util.MethodHelper;

/**
 * Transform the method body, creating continuations at blocking or continuing
 * method call sites.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class ContinuationTransformer extends BodyTransformer {
    public class ContinuationSite {

        public final int location;
        public final Unit continuedCall;
        public final SootClass frameClass;
        public final List<Local> locals;
        private final UnitBox callSite = Jimple.v().newStmtBox(null);
        private final Type returnType;
        private Local frame;

        private ContinuationSite(String id, int s, Unit u, List<Local> l,
                                 SootClass definingClass, Type t) {
            location = s;
            continuedCall = u;
            locals = l;
            frameClass = generateFrame(id, definingClass);
            returnType = t;
        }

        private Unit defaultReturn(Type returnType) {
            if (returnType == VoidType.v()) {
                return Jimple.v().newReturnVoidStmt();
            }
            if (returnType instanceof RefType
                || returnType instanceof ArrayType) {
                return Jimple.v().newReturnStmt(NullConstant.v());
            }

            if (returnType instanceof BooleanType) {
                return Jimple.v().newReturnStmt(IntConstant.v(0));
            }
            if (returnType instanceof ByteType) {
                return Jimple.v().newReturnStmt(IntConstant.v(0));
            }
            if (returnType instanceof CharType) {
                return Jimple.v().newReturnStmt(IntConstant.v(0));
            }
            if (returnType instanceof DoubleType) {
                return Jimple.v().newReturnStmt(DoubleConstant.v(0));
            }
            if (returnType instanceof FloatType) {
                return Jimple.v().newReturnStmt(FloatConstant.v(0));
            }
            if (returnType instanceof IntType) {
                return Jimple.v().newReturnStmt(IntConstant.v(0));
            }
            if (returnType instanceof LongType) {
                return Jimple.v().newReturnStmt(LongConstant.v(0));
            }
            if (returnType instanceof ShortType) {
                return Jimple.v().newReturnStmt(IntConstant.v(0));
            }
            throw new VerifyError(String.format("Invalid return type: %1s",
                                                returnType));
        }

        private SootClass generateFrame(String id, SootClass definingClass) {
            if (locals.size() == 0) {
                return emptyContinuationFrame;
            }
            StringBuffer generatedName = new StringBuffer(100);
            generatedName.append(definingClass.getName()).append('$').append(id).append('$').append(location).append("$gen");
            SootClass frame = new SootClass(generatedName.toString(),
                                            Modifier.PUBLIC);
            frame.setSuperclass(continuationFrame);
            int i = 0;
            for (Local local : locals) {
                frame.addField(new SootField(LOCAL_PREFIX + i++,
                                             local.getType(), Modifier.PUBLIC));
            }
            SootMethod constructor = continuationFrame.getMethod(NO_ARG_CONSTRUCTOR_SIGNATURE);
            @SuppressWarnings("unchecked")
			MethodHelper helper = new MethodHelper(
                                                   frame,
                                                   constructor.getName(),
                                                   constructor.getParameterTypes(),
                                                   VoidType.v(),
                                                   constructor.getModifiers());
            helper.invoke(Jimple.v().newSpecialInvokeExpr(helper.thisLocal(),
                                                          constructor.makeRef()));
            helper.returnVoid();

            frame.addMethod(helper.getMethod());
            Scene.v().addClass(frame);
            generated.add(frame);
            return frame;
        }

        private List<Unit> popFrame(Body body, Local poppedFrame) {
            ArrayList<Unit> pop = new ArrayList<Unit>();
            Jimple jimple = Jimple.v();
            pop.add(jimple.newAssignStmt(frame,
                                         jimple.newCastExpr(poppedFrame,
                                                            frameClass.getType())));
            int index = 0;
            for (Local l : locals) {
                pop.add(jimple.newAssignStmt(l,
                                             jimple.newInstanceFieldRef(frame,
                                                                        frameClass.getFieldByName(LOCAL_PREFIX
                                                                                                          + index++).makeRef())));
            }
            pop.add(jimple.newGotoStmt(callSite));
            return pop;
        }

        private List<Unit> pushFrame(Body body, Local frame) {
            ArrayList<Unit> push = new ArrayList<Unit>();
            Jimple jimple = Jimple.v();

            // <FrameClass> frame = new <FrameClass>();
            push.add(jimple.newAssignStmt(frame,
                                          jimple.newNewExpr(frameClass.getType())));
            push.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(frame,
                                                                      frameClass.getMethod(NO_ARG_CONSTRUCTOR_SIGNATURE).makeRef())));

            InstanceFieldRef ref = jimple.newInstanceFieldRef(frame,
                                                              frameClass.getSuperclass().getFieldByName(CONTINUATON_LOCATION_FIELD).makeRef());
            push.add(jimple.newAssignStmt(ref, IntConstant.v(location)));

            // Load the live locals into the frame instance variables
            int index = 0;
            for (Local l : locals) {
                String instVar = LOCAL_PREFIX + index++;
                ref = jimple.newInstanceFieldRef(frame,
                                                 frameClass.getFieldByName(instVar).makeRef());
                push.add(jimple.newAssignStmt(ref, l));
            }
            return push;
        }

        private void transformContinuation(Body body, Local poppedFrame) {
            PatchingChain<Unit> units = body.getUnits();
            Jimple jimple = Jimple.v();
            Local saveFrame = newLocal(SAVE_FRAME_LOCAL, BooleanType.v(), body);
            frame = newLocal(FRAME_LOCAL + location, frameClass.getType(), body);

            UnitBox next = jimple.newStmtBox(null);
            ArrayList<Unit> post = new ArrayList<Unit>();
            post.add(jimple.newAssignStmt(saveFrame,
                                          jimple.newStaticInvokeExpr(framework.getMethod(FRAMEWORK_SAVE_FRAME_SIGNATURE).makeRef())));
            post.add(jimple.newIfStmt(jimple.newEqExpr(saveFrame,
                                                       IntConstant.v(0)), next));

            post.add(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(framework.getMethod(PUSH_FRAME_SIGNATURE).makeRef(),
                                                                     asList(frame))));
            post.add(defaultReturn(returnType));

            Unit nextUnit = units.getSuccOf(continuedCall);
            units.insertBefore(pushFrame(body, frame), continuedCall);
            units.insertAfter(post, continuedCall);
            callSite.setUnit(continuedCall);
            next.setUnit(nextUnit);
        }
    }

    private static final String SAVE_FRAME_LOCAL = "saveFrame";
    private static final String FRAME_LOCAL = "frame";
    private static final String FRAMEWORK_SAVE_FRAME_SIGNATURE = "boolean saveFrame()";
    private static final String CONTINUATION_FRAME_LOCAL = "continuationFrame";
    private static final String INVALID_LOCATION_LOCAL = "invalidLocation";
    private static final String LOCATION_LOCAL = "location";
    private static final String RESTORE_FRAME_LOCAL = "restoreFrame";
    private static final String NO_ARG_CONSTRUCTOR_SIGNATURE = "void <init>()";
    private static final String FRAMEWORK_RESTORE_FRAME_SIGNATURE = "boolean restoreFrame()";
    private static final String PUSH_FRAME_SIGNATURE = "void pushFrame(com.hellblazer.primeMover.runtime.ContinuationFrame)";
    private static final String POP_FRAME_SIGNATURE = "com.hellblazer.primeMover.runtime.ContinuationFrame popFrame()";
    private static final String LOCAL_PREFIX = "l_";
    private static final String CONTINUATON_LOCATION_FIELD = LOCATION_LOCAL;
    private static final Logger log = Logger.getLogger(ContinuationTransformer.class.getCanonicalName());

    private static Local newLocal(String name, Type t, Body b) {
        Local l = Jimple.v().newLocal(name, t);
        b.getLocals().add(l);
        return l;
    }

    private final SootClass framework = Scene.v().loadClassAndSupport("com.hellblazer.primeMover.runtime.Framework");
    private final SootClass continuationFrame = Scene.v().loadClassAndSupport("com.hellblazer.primeMover.runtime.ContinuationFrame");
    private final SootClass illegalStateException = Scene.v().loadClassAndSupport(IllegalStateException.class.getCanonicalName());
    private final SootClass emptyContinuationFrame = Scene.v().loadClassAndSupport("com.hellblazer.primeMover.runtime.EmptyContinuationFrame");
    private final boolean validate;
    private final List<SootClass> generated;

    public ContinuationTransformer(List<SootClass> generated) {
        this(generated, false);
    }

    public ContinuationTransformer(List<SootClass> generated, boolean validate) {
        this.validate = validate;
        this.generated = generated;
    }

    public Local getThisLocal(Body body) {
        for (Unit unit : body.getUnits()) {
            if (unit instanceof IdentityStmt
                && ((IdentityStmt) unit).getRightOp() instanceof ThisRef) {
                return (Local) ((IdentityStmt) unit).getLeftOp();
            }
        }
        return null;
    }

    private List<Unit> generateContinuableReentry(Body body,
                                                  Local poppedFrame,
                                                  List<ContinuationSite> continuations) {
        ArrayList<Unit> reentry = new ArrayList<Unit>();
        Jimple jimple = Jimple.v();

        UnitBox normalEntry = jimple.newStmtBox(jimple.newNopStmt());
        Local restoreFrame = newLocal(RESTORE_FRAME_LOCAL, BooleanType.v(),
                                      body);

        // boolean restoreFramew = Framework.restoreFrame();
        reentry.add(jimple.newAssignStmt(restoreFrame,
                                         jimple.newStaticInvokeExpr(framework.getMethod(FRAMEWORK_RESTORE_FRAME_SIGNATURE).makeRef())));
        // if (restoreFrame) {
        reentry.add(jimple.newIfStmt(jimple.newEqExpr(restoreFrame,
                                                      IntConstant.v(0)),
                                     normalEntry));
        reentry.add(jimple.newAssignStmt(poppedFrame,
                                         jimple.newStaticInvokeExpr(framework.getMethod(POP_FRAME_SIGNATURE).makeRef())));
        Local location = newLocal(LOCATION_LOCAL, IntType.v(), body);
        InstanceFieldRef ref = jimple.newInstanceFieldRef(poppedFrame,
                                                          continuationFrame.getFieldByName(CONTINUATON_LOCATION_FIELD).makeRef());
        reentry.add(jimple.newAssignStmt(location, ref));

        UnitBox reentryBranch = jimple.newStmtBox(null);
        reentry.add(jimple.newGotoStmt(reentryBranch));

        ArrayList<Value> lookupValues = new ArrayList<Value>();
        ArrayList<Unit> targets = new ArrayList<Unit>();

        for (ContinuationSite site : continuations) {
            List<Unit> popFrame = site.popFrame(body, poppedFrame);
            reentry.addAll(popFrame);

            lookupValues.add(IntConstant.v(site.location));
            targets.add(popFrame.get(0));
        }

        Local ise = newLocal(INVALID_LOCATION_LOCAL,
                             illegalStateException.getType(), body);

        Unit defaultTarget = jimple.newAssignStmt(ise,
                                                  jimple.newNewExpr(illegalStateException.getType()));
        reentry.add(defaultTarget);
        reentry.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(ise,
                                                                     illegalStateException.getMethod(NO_ARG_CONSTRUCTOR_SIGNATURE).makeRef())));
        reentry.add(jimple.newThrowStmt(ise));

        reentryBranch.setUnit(jimple.newLookupSwitchStmt(location,
                                                         lookupValues, targets,
                                                         defaultTarget));
        reentry.add(reentryBranch.getUnit());
        reentry.add(normalEntry.getUnit());
        return reentry;
    }

    @Override
    protected void internalTransform(Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
        if (!willContinue(body.getMethod())) {
            return;
        }
        List<ContinuationSite> continuedCalls = generateContinuationSites(body,
                                                                          body.getMethod().getReturnType());
        if (continuedCalls.size() != 0) {
            Local poppedFrame = newLocal(CONTINUATION_FRAME_LOCAL,
                                         continuationFrame.getType(), body);
            for (ContinuationSite site : continuedCalls) {
                site.transformContinuation(body, poppedFrame);
            }
            body.getUnits().insertBefore(generateContinuableReentry(body,
                                                                    poppedFrame,
                                                                    continuedCalls),
                                         ((JimpleBody) body).getFirstNonIdentityStmt());
            markTransformed(body.getMethod(), this, "Transformed continuations");
            if (validate) {
                body.validate();
            }
        }
    }

    @SuppressWarnings("unchecked")
	List<ContinuationSite> generateContinuationSites(Body body, Type returnType) {
        String id = UUID.randomUUID().toString().replace('-', '_');
        UnusedLocalEliminator.v().transform(body);
        List<ContinuationSite> continuedCalls = new ArrayList<ContinuationSite>();
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(
                                                           new ExceptionalUnitGraph(
                                                                                    body));
        int callSite = 0;
        @SuppressWarnings("rawtypes")
		Iterator statements = body.getUnits().snapshotIterator();
        Local thisLocal = body.getMethod().isStatic() ? null
                                                     : body.getThisLocal();
        while (statements.hasNext()) {
            Stmt stmt = (Stmt) statements.next();
            SootClass declaringClass = body.getMethod().getDeclaringClass();
            List<Local> locals = liveLocals.getLiveLocalsBefore(stmt);
            if (thisLocal != null && locals.contains(thisLocal)) {
                locals = new ArrayList<Local>(locals);
                locals.remove(thisLocal);
                locals = Collections.unmodifiableList(locals);
            }
            if (stmt instanceof InvokeStmt) {
                InvokeStmt invoke = (InvokeStmt) stmt;
                if (willContinue(invoke.getInvokeExpr().getMethod())) {
                    ContinuationSite site = new ContinuationSite(
                                                                 id,
                                                                 callSite++,
                                                                 stmt,
                                                                 locals,
                                                                 declaringClass,
                                                                 returnType);
                    continuedCalls.add(site);
                }
            } else if (stmt instanceof AssignStmt) {
                AssignStmt assign = (AssignStmt) stmt;
                if (assign.containsInvokeExpr()
                    && willContinue(assign.getInvokeExpr().getMethod())) {
                    ContinuationSite site = new ContinuationSite(
                                                                 id,
                                                                 callSite++,
                                                                 stmt,
                                                                 locals,
                                                                 declaringClass,
                                                                 returnType);
                    continuedCalls.add(site);
                }
            } else if (stmt.containsInvokeExpr()) {
                String errorMessage = String.format("Found an expression which contains a method invocation that is not an assignment nor a stand alone invocation: %1s",
                                                    stmt);
                log.severe(errorMessage);
                throw new VerifyError(errorMessage);
            }
        }
        return continuedCalls;
    }
}
