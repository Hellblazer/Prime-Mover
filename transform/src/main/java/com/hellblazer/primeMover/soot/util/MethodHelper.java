package com.hellblazer.primeMover.soot.util;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.VoidType;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StaticFieldRef;
import soot.util.Chain;

public class MethodHelper {
    private static List<String> generateParameters(List<? extends Type> parameterTypes) {
        ArrayList<String> parameterNames = new ArrayList<String>();
        for (int i = 0; i < parameterTypes.size(); i++) {
            parameterNames.add("parameter" + i);
        }
        return parameterNames;
    }

    private final SootClass hostClass;
    private final SootMethod method;
    private final JimpleBody body;
    private final Chain<Unit> units;

    private final Jimple jimple = Jimple.v();
    private Local thisLocal;

    private Local[] parameters;
    private final SootClass boxedBoolean = Scene.v().loadClass(Boolean.class.getCanonicalName(),
                                                               SootClass.SIGNATURES);
    private final SootClass boxedByte = Scene.v().loadClass(Byte.class.getCanonicalName(),
                                                            SootClass.SIGNATURES);
    private final SootClass boxedChar = Scene.v().loadClass(Character.class.getCanonicalName(),
                                                            SootClass.SIGNATURES);
    private final SootClass boxedDouble = Scene.v().loadClass(Double.class.getCanonicalName(),
                                                              SootClass.SIGNATURES);
    private final SootClass boxedFloat = Scene.v().loadClass(Float.class.getCanonicalName(),
                                                             SootClass.SIGNATURES);
    private final SootClass boxedInt = Scene.v().loadClass(Integer.class.getCanonicalName(),
                                                           SootClass.SIGNATURES);
    private final SootClass boxedLong = Scene.v().loadClass(Long.class.getCanonicalName(),
                                                            SootClass.SIGNATURES);
    private final SootClass boxedShort = Scene.v().loadClass(Short.class.getCanonicalName(),
                                                             SootClass.SIGNATURES);

    private final SootClass object = Scene.v().loadClass(Object.class.getCanonicalName(),
                                                         SootClass.SIGNATURES);

    public MethodHelper(SootClass clazz, int modifiers) {
        this(clazz, "<init>", modifiers);
    }

    public MethodHelper(SootClass clazz, List<? extends Type> parameterTypes,
                        int modifiers) {
        this(clazz, "<init>", generateParameters(parameterTypes),
             parameterTypes, VoidType.v(), modifiers);
    }

    public MethodHelper(SootClass clazz, List<? extends Type> parameterTypes,
                        Type returnType, int modifiers) {
        this(clazz, "<init>", generateParameters(parameterTypes),
             parameterTypes, returnType, modifiers);
    }

    public MethodHelper(SootClass clazz, List<String> parameterNames,
                        List<? extends Type> parameterTypes, int modifiers) {
        this(clazz, "<init>", parameterNames, parameterTypes, VoidType.v(),
             modifiers);
    }

    @SuppressWarnings("unchecked")
	public MethodHelper(SootClass clazz, String name, int modifiers) {
        this(clazz, name, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
             VoidType.v(), modifiers);
    }

    public MethodHelper(SootClass clazz, String name,
                        List<? extends Type> parameterTypes, Type returnType,
                        int modifiers) {
        this(clazz, name, generateParameters(parameterTypes), parameterTypes,
             returnType, modifiers);
    }

    public MethodHelper(SootClass clazz, String name,
                        List<String> parameterNames,
                        List<? extends Type> parameterTypes, Type returnType,
                        int modifiers) {
        if (clazz == null) {
            throw new NullPointerException("defining class must not be null");
        }
        if (name == null) {
            throw new NullPointerException("method name must not be null");
        }
        if (parameterNames == null) {
            throw new NullPointerException("parameter names must not be null");
        }
        if (parameterTypes == null) {
            throw new NullPointerException("parameter types must not be null");
        }
        if (parameterNames.size() != parameterTypes.size()) {
            throw new IllegalArgumentException(
                                               "The number of parameter names must be equal to the number of parameters");
        }
        if (modifiers < 0) {
            throw new IllegalArgumentException("Modifier must be > 0");
        }

        hostClass = clazz;
        method = new SootMethod(name, parameterTypes, returnType);
        method.setModifiers(modifiers);

        body = jimple.newBody(method);
        method.setActiveBody(body);
        units = body.getUnits();

        if (!method.isStatic()) {
            thisLocal = newLocal("this", hostClass.getType());
            units.add(jimple.newIdentityStmt(thisLocal,
                                             jimple.newThisRef(RefType.v(hostClass))));
        }
        parameters = new Local[method.getParameterCount()];
        int i = 0;
        for (String param : parameterNames) {
            Local arg = newLocal(param, method.getParameterType(i));
            units.add(jimple.newIdentityStmt(arg,
                                             jimple.newParameterRef(method.getParameterType(i),
                                                                    i)));
            parameters[i++] = arg;
        }
    }

    @SuppressWarnings("unchecked")
	public MethodHelper(SootClass clazz, String name, Type returnType,
                        int modifiers) {
        this(clazz, name, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
             returnType, modifiers);
    }

    public void add(Unit unit) {
        units.add(unit);
    }

    public void addAll(Collection<Unit> units) {
        units.addAll(units);
    }

    public void assign(Value variable, Value rValue) {
        units.add(jimple.newAssignStmt(variable, rValue));
    }

    public void assignInstanceVariable(String instanceVariable, Value rValue) {
        if (method.isStatic()) {
            throw new UnsupportedOperationException(
                                                    "Cannot assign an instance variable in a static method");
        }
        InstanceFieldRef ref = getInstanceVariableRef(instanceVariable);
        units.add(jimple.newAssignStmt(ref, rValue));
    }

    public void assignInstanceVariableTo(Local target, String instanceVariable) {
        if (method.isStatic()) {
            throw new UnsupportedOperationException(
                                                    "Cannot retrieve an instance variable in a static method");
        }
        InstanceFieldRef ref = getInstanceVariableRef(instanceVariable);
        units.add(jimple.newAssignStmt(target, ref));
    }

    public void assignStaticVariable(String staticVariable, Value rValue) {
        StaticFieldRef ref = getStaticFieldRef(staticVariable);
        units.add(jimple.newAssignStmt(ref, rValue));
    }

    public void assignStaticVariableTo(Local target, String staticVariable) {
        StaticFieldRef ref = getStaticFieldRef(staticVariable);
        units.add(jimple.newAssignStmt(target, ref));
    }

    public Local box(PrimType primType, Local unboxedSource) {

        Local boxedResult;
        if (primType instanceof BooleanType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedBoolean.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedBoolean.getMethod("java.lang.Boolean valueOf(boolean)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof ByteType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedByte.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedByte.getMethod("java.lang.Byte valueOf(byte)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof CharType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedChar.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedChar.getMethod("java.lang.Character valueOf(char)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof DoubleType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedDouble.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedDouble.getMethod("java.lang.Double valueOf(double)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof FloatType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedFloat.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedFloat.getMethod("java.lang.Float valueOf(float)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof IntType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedInt.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedInt.getMethod("java.lang.Integer valueOf(int)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof LongType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedLong.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedLong.getMethod("java.lang.Long valueOf(long)").makeRef(),
                                                                      asList(unboxedSource))));

        } else if (primType instanceof ShortType) {
            boxedResult = newLocal(UUID.randomUUID().toString(),
                                   boxedShort.getType());
            units.add(jimple.newAssignStmt(boxedResult,
                                           jimple.newStaticInvokeExpr(boxedShort.getMethod("java.lang.Short valueOf(short)").makeRef(),
                                                                      asList(unboxedSource))));

        } else {
            throw new IllegalArgumentException(
                                               "Parameter type is not a valid primitive type: "
                                                       + primType);
        }
        return boxedResult;
    }

    public UnitBox generateIf(Value condition) {
        UnitBox target = jimple.newStmtBox(null);
        units.add(jimple.newIfStmt(condition, target));
        return target;
    }

    public InstanceFieldRef getInstanceVariableRef(String instanceVariable) {
        if (method.isStatic()) {
            throw new UnsupportedOperationException(
                                                    "Cannot retrieve an instance variable in a static method");
        }
        return jimple.newInstanceFieldRef(thisLocal,
                                          hostClass.getFieldByName(instanceVariable).makeRef());
    }

    public Unit getLast() {
        return units.getLast();
    }

    public SootMethod getMethod() {
        return method;
    }

    public Local getParameter(int index) {
        if (index > parameters.length) {
            throw new IllegalArgumentException("Invalid parameter index: "
                                               + index);
        }
        return parameters[index];
    }

    public List<Local> getParameters() {
        return asList(parameters);
    }

    public StaticFieldRef getStaticFieldRef(String staticVariable) {
        return jimple.newStaticFieldRef(hostClass.getFieldByName(staticVariable).makeRef());
    }

    public void invoke(Value op) {
        units.add(jimple.newInvokeStmt(op));
    }

    /**
     * Load all the method arguments on the stack, as a single object array,
     * into the target local;
     */
    public void loadArgumentsArray(Local arguments) {
        units.add(jimple.newAssignStmt(arguments,
                                       jimple.newNewArrayExpr(object.getType(),
                                                              IntConstant.v(parameters.length))));

        for (int i = 0; i < method.getParameterCount(); i++) {
            Type paramType = method.getParameterType(i);
            ArrayRef slot = jimple.newArrayRef(arguments, IntConstant.v(i));
            if (paramType instanceof PrimType) {
                Local boxedArg = box((PrimType) paramType, parameters[i]);
                units.add(jimple.newAssignStmt(slot, boxedArg));
            } else {
                units.add(jimple.newAssignStmt(slot, parameters[i]));
            }
        }
    }

    @SuppressWarnings("unchecked")
	public void newInstance(Local target, SootClass clazz) {
        newInstance(target, clazz, Collections.EMPTY_LIST, new Value[] {});
    }

    public void newInstance(Local target, SootClass clazz,
                            List<Type> parameterTypes, Value... arguments) {

        units.add(jimple.newAssignStmt(target,
                                       jimple.newNewExpr(clazz.getType())));
        units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(target,
                                                                   clazz.getMethod("<init>",
                                                                                   asList(arguments)).makeRef())));
    }

    public Local newLocal(String name, Type type) {
        Local local = jimple.newLocal(name, type);
        body.getLocals().add(local);
        return local;
    }

    public void returnBoxed(Type returnType, Local unboxedValue) {
        if (returnType instanceof RefType || returnType instanceof ArrayType) {
            units.add(jimple.newReturnStmt(unboxedValue));
            return;
        }

        if (!(returnType instanceof PrimType)) {
            throw new IllegalStateException("Not a valid boxed return type: "
                                            + returnType);
        }

        Local returnValue = box((PrimType) returnType, unboxedValue);
        units.add(Jimple.v().newReturnStmt(returnValue));
        return;
    }

    public void returnUnboxed(Type returnType, Local boxedValue) {
        if (returnType instanceof VoidType) {
            units.add(jimple.newReturnVoidStmt());
            return;
        }

        if (returnType instanceof RefType || returnType instanceof ArrayType) {
            Local returnValue = newLocal(UUID.randomUUID().toString(),
                                         returnType);
            units.add(jimple.newAssignStmt(returnValue,
                                           jimple.newCastExpr(boxedValue,
                                                              returnType)));
            units.add(jimple.newReturnStmt(returnValue));
            return;
        }

        if (!(returnType instanceof PrimType)) {
            throw new IllegalStateException("Not a valid return type: "
                                            + returnType);
        }

        Local returnValue = newLocal(UUID.randomUUID().toString(), returnType);
        unbox((PrimType) returnType, returnValue, boxedValue);
        units.add(Jimple.v().newReturnStmt(returnValue));
        return;
    }

    public void returnValue(Value returnValue) {
        units.add(jimple.newReturnStmt(returnValue));
    }

    public void returnVoid() {
        units.add(jimple.newReturnVoidStmt());
    }

    public Local thisLocal() {
        if (method.isStatic()) {
            throw new UnsupportedOperationException(
                                                    "Cannot retrieve 'this' in a static method");
        }
        return thisLocal;
    }

    public void unbox(PrimType primType, Local castResult, Local boxedSource) {

        if (primType instanceof BooleanType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedBoolean.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedBoolean.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedBoolean.getMethod("boolean booleanValue()").makeRef())));
        } else if (primType instanceof ByteType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedByte.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedByte.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedByte.getMethod("byte byteValue()").makeRef())));

        } else if (primType instanceof CharType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedChar.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedChar.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedChar.getMethod("char charValue()").makeRef())));
        } else if (primType instanceof DoubleType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedDouble.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedDouble.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedDouble.getMethod("double doubleValue()").makeRef())));
        } else if (primType instanceof FloatType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedFloat.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedFloat.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedFloat.getMethod("float floatValue()").makeRef())));
        } else if (primType instanceof IntType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedInt.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedInt.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedInt.getMethod("int intValue()").makeRef())));
        } else if (primType instanceof LongType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedLong.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedLong.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedLong.getMethod("long longValue()").makeRef())));
        } else if (primType instanceof ShortType) {
            Local boxLocal = newLocal(UUID.randomUUID().toString(),
                                      boxedShort.getType());
            units.add(jimple.newAssignStmt(boxLocal,
                                           jimple.newCastExpr(boxedSource,
                                                              boxedShort.getType())));
            units.add(jimple.newAssignStmt(castResult,
                                           jimple.newVirtualInvokeExpr(boxLocal,
                                                                       boxedShort.getMethod("short shortValue()").makeRef())));
        } else {
            throw new IllegalArgumentException(
                                               "Parameter type is not a valid primitive type: "
                                                       + primType);
        }
    }

    public void validate() {
        method.getActiveBody().validate();
    }
}
