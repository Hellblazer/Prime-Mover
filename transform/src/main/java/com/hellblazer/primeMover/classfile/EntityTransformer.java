package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.asm.OpenAddressingSet;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hal.hildebrand
 **/
public class EntityTransformer {

    private static final String    APPEND                    = "append";
    private static final Method    APPEND_METHOD;
    private static final String    BOOLEAN_VALUE             = "booleanValue";
    private static final Method    BOOLEAN_VALUE_METHOD;
    private static final Method    BOOLEAN_VALUE_OF_METHOD;
    private static final String    BYTE_VALUE                = "byteValue";
    private static final Method    BYTE_VALUE_METHOD;
    private static final Method    BYTE_VALUE_OF_METHOD;
    private static final String    CHARACTER_VALUE           = "charValue";
    private static final Method    CHARACTER_VALUE_METHOD;
    private static final Method    CHARACTER_VALUE_OF_METHOD;
    private static final String    DOUBLE_VALUE              = "doubleValue";
    private static final Method    DOUBLE_VALUE_METHOD;
    private static final Method    DOUBLE_VALUE_OF_METHOD;
    private static final String    FLOAT_VALUE               = "floatValue";
    private static final Method    FLOAT_VALUE_METHOD;
    private static final Method    FLOAT_VALUE_OF_METHOD;
    private static final String    GET_CONTROLLER            = "getController";
    private static final Method    GET_CONTROLLER_METHOD;
    private static final String    INT_VALUE                 = "intValue";
    private static final Method    INT_VALUE_METHOD;
    private static final Method    INTEGER_VALUE_OF_METHOD;
    private static final String    INVOKE                    = "__invoke";
    private static final Method    INVOKE_METHOD;
    private static final String    LONG_VALUE                = "longValue";
    private static final Method    LONG_VALUE_METHOD;
    private static final Method    LONG_VALUE_OF_METHOD;
    private static final String    METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final ClassDesc OBJECT_TYPE               = ClassDesc.of(Object.class.getCanonicalName());
    private static final String    POST_CONTINUING_EVENT     = "postContinuingEvent";
    private static final Method    POST_CONTINUING_EVENT_METHOD;
    private static final String    POST_EVENT                = "postEvent";
    private static final Method    POST_EVENT_METHOD;
    private static final String    REMAPPED_TEMPLATE         = "%s$event";
    private static final String    SHORT_VALUE               = "shortValue";
    private static final Method    SHORT_VALUE_METHOD;
    private static final Method    SHORT_VALUE_OF_METHOD;
    private static final String    SIGNATURE_FOR             = "__signatureFor";
    private static final Method    SIGNATURE_FOR_METHOD;
    private static final Method    STRING_BUILDER_CONSTRUCTOR;
    private static final ClassDesc STRING_BUILDER_TYPE       = ClassDesc.of(StringBuilder.class.getCanonicalName());
    private static final String    TO_STRING                 = "toString";
    private static final Method    TO_STRING_METHOD;
    private static final String    VALUE_OF                  = "valueOf";

    static {
        java.lang.reflect.Method method;
        try {
            method = Devi.class.getMethod(POST_CONTINUING_EVENT, EntityReference.class, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_CONTINUING_EVENT), e);
        }
        try {
            POST_CONTINUING_EVENT_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_CONTINUING_EVENT), e);
        }
        try {
            method = Devi.class.getMethod(POST_EVENT, EntityReference.class, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_EVENT), e);
        }
        try {
            POST_EVENT_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(POST_EVENT), e);
        }
        try {
            method = Framework.class.getMethod(GET_CONTROLLER);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(GET_CONTROLLER), e);
        }
        try {
            GET_CONTROLLER_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(GET_CONTROLLER), e);
        }
        try {
            method = EntityReference.class.getMethod(SIGNATURE_FOR, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        try {
            SIGNATURE_FOR_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        try {
            method = EntityReference.class.getMethod(INVOKE, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
        try {
            INVOKE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
        try {
            method = Boolean.class.getMethod(VALUE_OF, boolean.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            BOOLEAN_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Character.class.getMethod(VALUE_OF, char.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            CHARACTER_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Byte.class.getMethod(VALUE_OF, byte.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            BYTE_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Double.class.getMethod(VALUE_OF, double.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            DOUBLE_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Float.class.getMethod(VALUE_OF, float.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            FLOAT_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Integer.class.getMethod(VALUE_OF, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            INTEGER_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Long.class.getMethod(VALUE_OF, long.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            LONG_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            method = Short.class.getMethod(VALUE_OF, short.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }
        try {
            SHORT_VALUE_OF_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(VALUE_OF), e);
        }

        try {
            method = Boolean.class.getMethod(BOOLEAN_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BOOLEAN_VALUE), e);
        }
        try {
            BOOLEAN_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BOOLEAN_VALUE), e);
        }
        try {
            method = Character.class.getMethod(CHARACTER_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(CHARACTER_VALUE), e);
        }
        try {
            CHARACTER_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(CHARACTER_VALUE), e);
        }
        try {
            method = Byte.class.getMethod(BYTE_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BYTE_VALUE), e);
        }
        try {
            BYTE_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(BYTE_VALUE), e);
        }
        try {
            method = Double.class.getMethod(DOUBLE_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(DOUBLE_VALUE), e);
        }
        try {
            DOUBLE_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(DOUBLE_VALUE), e);
        }
        try {
            method = Float.class.getMethod(FLOAT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(FLOAT_VALUE), e);
        }
        try {
            FLOAT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(FLOAT_VALUE), e);
        }
        try {
            method = Integer.class.getMethod(INT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INT_VALUE), e);
        }
        try {
            INT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INT_VALUE), e);
        }
        try {
            method = Long.class.getMethod(LONG_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(LONG_VALUE), e);
        }
        try {
            LONG_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(LONG_VALUE), e);
        }
        try {
            method = Short.class.getMethod(SHORT_VALUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SHORT_VALUE), e);
        }
        try {
            SHORT_VALUE_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SHORT_VALUE), e);
        }
        try {
            method = StringBuilder.class.getMethod(APPEND, String.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(APPEND), e);
        }
        try {
            APPEND_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(APPEND), e);
        }
        try {
            method = StringBuilder.class.getMethod(TO_STRING);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(TO_STRING), e);
        }
        try {
            TO_STRING_METHOD = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(TO_STRING), e);
        }
        Constructor<StringBuilder> constructor;
        try {
            constructor = StringBuilder.class.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get constructor", e);
        }
        STRING_BUILDER_CONSTRUCTOR = Method.getMethod(constructor);
    }

    private final Set<Method>              blocking;
    private final ClassInfo                clazz;
    private final Set<Method>              events;
    private final String                   internalName;
    private final Map<Method, Integer>     inverse;
    private final Map<Integer, MethodInfo> mapped;
    private final Set<MethodInfo>          remapped;
    private final ClassDesc                type;

    public EntityTransformer(ClassInfo clazz, Set<MethodInfo> events) {
        this.clazz = clazz;
        type = ClassDesc.of(clazz.getName());
        internalName = clazz.getName().replace('.', '/');
        mapped = new HashMap<>();
        remapped = new OpenAddressingSet.OpenSet<>();
        blocking = new OpenAddressingSet.OpenSet<>();
        inverse = new HashMap<>();
        this.events = new OpenAddressingSet.OpenSet<>();
        var key = 0;
        for (var mi : events.stream().sorted().toList()) {
            mapped.put(key, mi);
            final var event = new Method(mi.getName(), MethodTypeDesc.ofDescriptor(mi.getTypeDescriptorStr()));
            inverse.put(event, key++);
            this.events.add(event);
            if (!mi.getTypeDescriptor().getResultType().toString().equals("void") || mi.hasAnnotation(Blocking.class)) {
                blocking.add(event);
            }
            final boolean declared = clazz.getDeclaredMethodInfo(mi.getName())
                                          .stream()
                                          .filter(m -> mi.equals(m))
                                          .findFirst()
                                          .isPresent();
            if (declared) {
                remapped.add(mi);
            }
        }
    }

    public byte[] transform(byte[] bytes) {
        CodeTransform codeTransform = (codeBuilder, e) -> {
            switch (e) {
                case InvokeInstruction i -> {
                    var method = new Method(i.name().stringValue(), i.typeSymbol());
                    if (!mapped.containsKey(method)) {
                        codeBuilder.invoke(i.opcode(), ClassDesc.of("Bar"), i.name().stringValue(), i.typeSymbol(),
                                           i.isInterface());
                    } else {
                        codeBuilder.accept(e);
                    }
                }
                default -> codeBuilder.accept(e);
            }
        };
        MethodTransform mt = (methodBuilder, methodElement) -> {

        };
        var methodTransform = MethodTransform.transformingCode(codeTransform);
        var classTransform = ClassTransform.transformingMethods(methodTransform);
        return ClassFile.of().transformClass(ClassFile.of().parse(bytes), classTransform);
    }

    private record Method(String name, MethodTypeDesc descriptor) {
        static Method getMethod(final java.lang.reflect.Method method) {
            return new Method(method.getName(),
                              MethodTypeDesc.of(ClassDesc.of(method.getReturnType().getCanonicalName()), Arrays.stream(
                              method.getParameterTypes()).map(e -> ClassDesc.of(e.getCanonicalName())).toList()));
        }

        public static Method getMethod(Constructor<?> constructor) {
            return new Method("<init>",
                              MethodTypeDesc.of(ClassDesc.of(constructor.getDeclaringClass().getCanonicalName()),
                                                Arrays.stream(constructor.getParameterTypes())
                                                      .map(e -> ClassDesc.of(e.getCanonicalName()))
                                                      .toList()));
        }
    }
}
