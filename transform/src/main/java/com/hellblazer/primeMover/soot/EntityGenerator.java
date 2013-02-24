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

import static com.hellblazer.primeMover.soot.util.Utils.getEntityInterfaces;
import static com.hellblazer.primeMover.soot.util.Utils.isMarkedBlocking;
import static com.hellblazer.primeMover.soot.util.Utils.isMarkedNonEvent;
import static com.hellblazer.primeMover.soot.util.Utils.markBlocking;
import static com.hellblazer.primeMover.soot.util.Utils.markTransformed;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import soot.ArrayType;
import soot.BooleanType;
import soot.Immediate;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.VoidType;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;

import com.hellblazer.primeMover.soot.util.MethodHelper;

/**
 * Generates the subclass which will implement the mechanics of the event
 * processing logic for entities
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class EntityGenerator {
    private static Logger log = Logger.getLogger(EntityGenerator.class.getCanonicalName());
    private static final String INIT = "<init>";
    private static final String NO_ARG_CONSTRUCTOR_SIGNATURE = "void <init>()";
    private static final String FRAMEWORK_POST_EVENT_SIGNATURE = "void postEvent(com.hellblazer.primeMover.runtime.EntityReference,int,java.lang.Object[])";
    private static final String FRAMEWORK_POST_CONTINUING_EVENT_SIGNATURE = "java.lang.Object postContinuingEvent(com.hellblazer.primeMover.runtime.EntityReference,int,java.lang.Object[])";
    private static final String CLINIT = "<clinit>";
    public static final String CONTROLLER_FIELD = "__controller";
    public static final String SIGNATURE_FOR_METHOD_NAME = "__signatureFor";
    public static final String INITIALIZE_METHOD_NAME = "__initialize";
    public static final String INITIALIZED_FIELD = "__initialized";
    public static final String EVENT_MAP = "__EVENT_MAP";
    public static final String INVOKE_METHOD_NAME = "__invoke";
    public static final String GENERATED_ENTITY_SUFFIX = "$entity$gen";

    private final boolean validate;
    private final SootClass base;
    private final SootClass entity;
    private final Map<SootMethod, Integer> invokeMap = new HashMap<SootMethod, Integer>();
    private final Map<Integer, SootMethod> inverseInvokeMap = new TreeMap<Integer, SootMethod>();

    private final SootClass framework = Scene.v().loadClass("com.hellblazer.primeMover.runtime.Framework",
                                                            SootClass.SIGNATURES);
    private final SootClass devi = Scene.v().loadClass("com.hellblazer.primeMover.runtime.Devi",
                                                       SootClass.SIGNATURES);
    private final SootClass object = Scene.v().loadClass(Object.class.getCanonicalName(),
                                                         SootClass.SIGNATURES);
    private final SootClass string = Scene.v().loadClass(String.class.getCanonicalName(),
                                                         SootClass.SIGNATURES);
    private final Type stringArrayType = ArrayType.v(string.getType(), 1);
    private final SootClass entityReference = Scene.v().loadClass("com.hellblazer.primeMover.runtime.EntityReference",
                                                                  SootClass.SIGNATURES);
    private final SootClass noSuchMethodError = Scene.v().loadClass(NoSuchMethodError.class.getCanonicalName(),
                                                                    SootClass.SIGNATURES);

    public EntityGenerator(List<SootClass> generated, SootClass base) {
        this(generated, base, false);
    }

    public EntityGenerator(List<SootClass> generated, SootClass base,
                           boolean validate) {
        this.base = base;
        this.validate = validate;
        entity = new SootClass(base.getJavaPackageName() + "."
                               + base.getJavaStyleName()
                               + GENERATED_ENTITY_SUFFIX, Modifier.PUBLIC);
        Scene.v().addClass(entity);
        generated.add(entity);
        entity.setSuperclass(base);
        populateFields();
    }

    public void generateEntity() {
        constructInvokeMap();
        generateClassInitMethod();
        SootMethod initializeMethod = generateInitializeMethod();
        generateSignatureForMethod();
        generateConstructors(initializeMethod);
        generateInvokeMethod();
        for (SootMethod event : invokeMap.keySet()) {
            generateEvent(event, initializeMethod);
        }
        markTransformed(base, this, "Generated entity proxy subclass");
    }

    public SootClass getBase() {
        return base;
    }

    public SootClass getEntity() {
        return entity;
    }

    private SootMethod findMethod(String subSignature) {
        SootClass current = base;
        while (base != null) {
            if (current.declaresMethod(subSignature)) {
                return current.getMethod(subSignature);
            }
            current = current.hasSuperclass() ? current.getSuperclass() : null;
        }
        throw new IllegalStateException(
                                        String.format("Cannot find concrete method %s in %s",
                                                      subSignature, base));
    }

    private int gatherAllMethods(Set<String> mapped, int index) {
        SootClass current = base;
        while (current != null && current != object) {
            for (SootMethod method : current.getMethods()) {
                if (!mapped.contains(method.getSubSignature())
                    && isEvent(method)) {
                    invokeMap.put(method, index);
                    inverseInvokeMap.put(index++, method);
                    mapped.add(method.getSubSignature());
                }
            }
            current = current.getSuperclass();
        }
        return index;
    }

    private int gatherMethodsForInterface(SootClass iFace, Set<String> mapped,
                                          int index) {
        for (SootMethod method : iFace.getMethods()) {
            String subSignature = method.getSubSignature();
            if (!mapped.contains(subSignature) && !isMarkedNonEvent(method)) {
                SootMethod concrete = findMethod(subSignature);
                if (!isMarkedNonEvent(concrete)) {
                    invokeMap.put(concrete, index);
                    inverseInvokeMap.put(index++, concrete);
                    mapped.add(subSignature);
                }
            }
        }
        return index;
    }

    private boolean isEvent(SootMethod method) {
        return method.isPublic() && method.isConcrete() && !method.isStatic()
               && !isInit(method) && !isMarkedNonEvent(method);
    }

    private boolean isInit(SootMethod method) {
        return method.getName().equals(INIT) || method.getName().equals(CLINIT);
    }

    protected void constructInvokeMap() {
        HashSet<String> mapped = new HashSet<String>();
        for (SootMethod method : object.getMethods()) {
            if (method.isPublic() && !method.isStatic() && !isInit(method)) {
                mapped.add(method.getSubSignature());
            }
        }
        int index = 0;
        Collection<SootClass> entityInterfaces = getEntityInterfaces(base);
        if (entityInterfaces.isEmpty()) {
            index = gatherAllMethods(mapped, index);
        } else {
            for (SootClass iFace : entityInterfaces) {
                index = gatherMethodsForInterface(iFace, mapped, index);
            }
        }
        for (SootMethod method : inverseInvokeMap.values()) {
            if (method.getReturnType() != VoidType.v()
                && !isMarkedBlocking(method)) {
                markBlocking(method);
                log.info(String.format("Inferred blocking status of %1s.  Marking method as @Blocking",
                                       method));
            }
        }
    }

    /**
     * Generate the static class initialization method. This method initializes
     * the map of event method signatures to their ordinal number.
     * 
     * Method body:
     * 
     * static { __EVENT_MAP = new HashMap<Integer, String>(); __EVENT_MAP.put(0,
     * "void event1()"); ...
     * 
     * __EVENT_MAP.put(N, "void eventN()"); }
     * 
     * @return
     */
    protected SootMethod generateClassInitMethod() {
        MethodHelper helper = new MethodHelper(entity, CLINIT,
                                               Modifier.STATIC
                                                       | Modifier.PRIVATE);
        Local map = helper.newLocal("map", stringArrayType);
        helper.assign(map,
                      Jimple.v().newNewArrayExpr(string.getType(),
                                                 IntConstant.v(inverseInvokeMap.size())));
        helper.assignStaticVariable(EVENT_MAP, map);
        for (Map.Entry<Integer, SootMethod> entry : inverseInvokeMap.entrySet()) {
            helper.assign(Jimple.v().newArrayRef(map,
                                                 IntConstant.v(entry.getKey())),
                          StringConstant.v(getSignature(entry.getValue())));
        }
        helper.returnVoid();
        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    protected SootMethod generateConstructor(SootMethod constructor,
                                             SootMethod initializeMethod) {
        @SuppressWarnings("unchecked")
		MethodHelper helper = new MethodHelper(entity, constructor.getName(),
                                               constructor.getParameterTypes(),
                                               VoidType.v(),
                                               constructor.getModifiers());
        helper.invoke(Jimple.v().newSpecialInvokeExpr(helper.thisLocal(),
                                                      constructor.makeRef(),
                                                      helper.getParameters()));
        helper.invoke(Jimple.v().newVirtualInvokeExpr(helper.thisLocal(),
                                                      initializeMethod.makeRef()));
        helper.returnVoid();
        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    protected List<SootMethod> generateConstructors(SootMethod initializeMethod) {
        ArrayList<SootMethod> constructors = new ArrayList<SootMethod>();
        for (SootMethod method : base.getMethods()) {
            if (INIT.equals(method.getName())) {
                constructors.add(generateConstructor(method, initializeMethod));
            }
        }
        return constructors;
    }

    protected SootMethod generateEvent(SootMethod event,
                                       SootMethod initializeMethod) {
        @SuppressWarnings("unchecked")
		MethodHelper helper = new MethodHelper(entity, event.getName(),
                                               event.getParameterTypes(),
                                               event.getReturnType(),
                                               event.getModifiers());

        Local initialized = helper.newLocal("initialized", BooleanType.v());
        helper.assignInstanceVariableTo(initialized, INITIALIZED_FIELD);
        UnitBox target = helper.generateIf(Jimple.v().newEqExpr(initialized,
                                                                IntConstant.v(1)));
        helper.invoke(Jimple.v().newVirtualInvokeExpr(helper.thisLocal(),
                                                      initializeMethod.makeRef()));

        Local controller = helper.newLocal("controller", devi.getType());
        helper.assignInstanceVariableTo(controller, CONTROLLER_FIELD);
        target.setUnit(helper.getLast());
        Local arguments = helper.newLocal("arguments",
                                          ArrayType.v(object.getType(), 1));
        if (event.getParameterCount() == 0) {
            helper.assign(arguments, NullConstant.v());
        } else {
            helper.loadArgumentsArray(arguments);
        }

        if (isMarkedBlocking(event)) {
            Local continuationReturnValue = helper.newLocal("continuationReturnValue",
                                                            object.getType());
            helper.assign(continuationReturnValue,
                          Jimple.v().newVirtualInvokeExpr(controller,
                                                          devi.getMethod(FRAMEWORK_POST_CONTINUING_EVENT_SIGNATURE).makeRef(),
                                                          asList(helper.thisLocal(),
                                                                 IntConstant.v(invokeMap.get(event)),
                                                                 arguments)));
            helper.returnUnboxed(event.getReturnType(), continuationReturnValue);
        } else {
            helper.invoke(Jimple.v().newVirtualInvokeExpr(controller,
                                                          devi.getMethod(FRAMEWORK_POST_EVENT_SIGNATURE).makeRef(),
                                                          asList(helper.thisLocal(),
                                                                 IntConstant.v(invokeMap.get(event)),
                                                                 arguments)));
            helper.returnVoid();
        }

        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    /**
     * Construct the method which initializes the entity.
     * 
     * method body:
     * 
     * private synchronized void __initialize() { if (__initialized) { return; }
     * __controller = Framework.getController(); __id = UUID.randomUUID();
     * __initialized = true; }
     * 
     * @return
     */
    protected SootMethod generateInitializeMethod() {
        @SuppressWarnings("unchecked")
		MethodHelper helper = new MethodHelper(entity, INITIALIZE_METHOD_NAME,
                                               Collections.EMPTY_LIST,
                                               VoidType.v(),
                                               Modifier.SYNCHRONIZED
                                                       | Modifier.PRIVATE);
        /*
         * if (__initialized) { return; }
         */

        Local initializedLocal = helper.newLocal("initialized", BooleanType.v());
        helper.assignInstanceVariableTo(initializedLocal, INITIALIZED_FIELD);
        UnitBox target = helper.generateIf(Jimple.v().newEqExpr(initializedLocal,
                                                                IntConstant.v(0)));
        helper.returnVoid();

        /*
         * __controller = Framework.getController();
         */
        SootMethod getController = framework.getMethod("getController",
                                                       Collections.emptyList(),
                                                       devi.getType());
        Local controllerLocal = helper.newLocal("controller", devi.getType());
        helper.assign(controllerLocal,
                      Jimple.v().newStaticInvokeExpr(getController.makeRef()));
        target.setUnit(helper.getLast());
        helper.assignInstanceVariable(CONTROLLER_FIELD, controllerLocal);

        /*
         * __initialized = true; return;
         */
        helper.assignInstanceVariable(INITIALIZED_FIELD, IntConstant.v(1));
        helper.returnVoid();
        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    protected Unit generateInvoke(SootMethod event, Local arguments,
                                  MethodHelper helper, String tempPrefix) {
        Unit first = Jimple.v().newNopStmt();
        helper.add(first);

        ArrayList<Local> castArguments = new ArrayList<Local>();

        for (int i = 0; i < event.getParameterCount(); i++) {
            Local rawArg = helper.newLocal(tempPrefix + "-raw" + i,
                                           object.getType());
            Type parameterType = event.getParameterType(i);
            Local arg = helper.newLocal(tempPrefix + i, parameterType);
            castArguments.add(arg);

            // Object rawArg = arguments[i];
            helper.assign(rawArg,
                          Jimple.v().newArrayRef(arguments, IntConstant.v(i)));
            if (parameterType instanceof PrimType) {
                // unbox instructions
                helper.unbox((PrimType) parameterType, arg, rawArg);
            } else {
                // <Type> arg = (<Type>) rawArg;
                helper.assign(arg,
                              Jimple.v().newCastExpr(rawArg, parameterType));
            }
        }

        if (event.getReturnType().equals(VoidType.v())) {
            helper.invoke(Jimple.v().newSpecialInvokeExpr(helper.thisLocal(),
                                                          event.makeRef(),
                                                          castArguments));
            helper.returnValue(NullConstant.v());
        } else {
            Local result = helper.newLocal("result", event.getReturnType());
            helper.assign(result,
                          Jimple.v().newSpecialInvokeExpr(helper.thisLocal(),
                                                          event.makeRef(),
                                                          castArguments));
            helper.returnBoxed(event.getReturnType(), result);
        }
        return first;
    }

    protected SootMethod generateInvokeMethod() {
        MethodHelper helper = new MethodHelper(
                                               entity,
                                               INVOKE_METHOD_NAME,
                                               asList("eventOrdinal",
                                                      "arguments"),
                                               asList(IntType.v(),
                                                      ArrayType.v(object.getType(),
                                                                  1)),
                                               object.getType(),
                                               Modifier.PUBLIC);
        UnitBox target = Jimple.v().newStmtBox(null);
        helper.add(Jimple.v().newGotoStmt(target));

        /**
         * switch (signatureOrdinal) { case 1: { result = this.event((ArgType)
         * arguments[0], (AnotherArgType) arguments[1]...); break; } ...
         * default: { throw new NoSuchMethodError(); } }
         */
        ArrayList<Immediate> lookupValues = new ArrayList<Immediate>();
        ArrayList<Unit> targets = new ArrayList<Unit>();
        String tempPrefix = "invoke$tmp$";
        for (Map.Entry<Integer, SootMethod> entry : inverseInvokeMap.entrySet()) {
            lookupValues.add(IntConstant.v(entry.getKey()));
            targets.add(generateInvoke(entry.getValue(),
                                       helper.getParameter(1), helper,
                                       tempPrefix + entry.getKey().intValue()));
        }

        Local nsmInstance = helper.newLocal("nsmInstance",
                                            noSuchMethodError.getType());

        // nsmInstance = new NoSuchMethodError();
        helper.assign(nsmInstance,
                      Jimple.v().newNewExpr(noSuchMethodError.getType()));
        Unit defaultTarget = helper.getLast();
        helper.invoke(Jimple.v().newSpecialInvokeExpr(nsmInstance,
                                                      noSuchMethodError.getMethod(NO_ARG_CONSTRUCTOR_SIGNATURE).makeRef(),
                                                      Collections.EMPTY_LIST));
        // throw nsmInstance;
        helper.add(Jimple.v().newThrowStmt(nsmInstance));

        helper.add(Jimple.v().newLookupSwitchStmt(helper.getParameter(0),
                                                  lookupValues, targets,
                                                  defaultTarget));
        target.setUnit(helper.getLast());
        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    protected SootMethod generateSignatureForMethod() {
        MethodHelper helper = new MethodHelper(entity,
                                               SIGNATURE_FOR_METHOD_NAME,
                                               asList(IntType.v()),
                                               string.getType(),
                                               Modifier.PUBLIC);

        Local eventMap = helper.newLocal("eventMap", stringArrayType);
        helper.assignStaticVariableTo(eventMap, EVENT_MAP);
        Local signature = helper.newLocal("signature", string.getType());
        helper.assign(signature,
                      Jimple.v().newArrayRef(eventMap, helper.getParameter(0)));
        helper.returnValue(signature);
        entity.addMethod(helper.getMethod());
        if (validate) {
            helper.validate();
        }
        return helper.getMethod();
    }

    protected String getSignature(SootMethod method) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<" + Scene.v().quotedNameOf(base.getName()) + ": ");
        buffer.append(method.getSubSignature());
        buffer.append(">");
        return buffer.toString();
    }

    protected void populateFields() {
        // private static HashMap __EVENT_METHOD_CACHE;
        entity.addField(new SootField(EVENT_MAP, stringArrayType,
                                      Modifier.STATIC | Modifier.PRIVATE));

        // private Kalachakra __controller;
        entity.addField(new SootField(CONTROLLER_FIELD, devi.getType(),
                                      Modifier.PRIVATE));

        // private boolean __initialized;
        entity.addField(new SootField(INITIALIZED_FIELD, BooleanType.v(),
                                      Modifier.PRIVATE));
        entity.addInterface(entityReference);
    }
}
