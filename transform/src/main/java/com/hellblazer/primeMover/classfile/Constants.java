package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.classfile.EntityTransformer.Method;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;

public class Constants {
    static String    APPEND                    = "append";
    static Method    APPEND_METHOD;
    static String    BOOLEAN_VALUE             = "booleanValue";
    static Method    BOOLEAN_VALUE_METHOD;
    static Method    BOOLEAN_VALUE_OF_METHOD;
    static String    BYTE_VALUE                = "byteValue";
    static Method    BYTE_VALUE_METHOD;
    static Method    BYTE_VALUE_OF_METHOD;
    static String    CHARACTER_VALUE           = "charValue";
    static Method    CHARACTER_VALUE_METHOD;
    static Method    CHARACTER_VALUE_OF_METHOD;
    static String    DOUBLE_VALUE              = "doubleValue";
    static Method    DOUBLE_VALUE_METHOD;
    static Method    DOUBLE_VALUE_OF_METHOD;
    static String    FLOAT_VALUE               = "floatValue";
    static Method    FLOAT_VALUE_METHOD;
    static Method    FLOAT_VALUE_OF_METHOD;
    static String    GET_CONTROLLER            = "getController";
    static Method    GET_CONTROLLER_METHOD;
    static String    INT_VALUE                 = "intValue";
    static Method    INT_VALUE_METHOD;
    static Method    INTEGER_VALUE_OF_METHOD;
    static String    INVOKE                    = "__invoke";
    static Method    INVOKE_METHOD;
    static String    LONG_VALUE                = "longValue";
    static Method    LONG_VALUE_METHOD;
    static Method    LONG_VALUE_OF_METHOD;
    static String    METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    static ClassDesc OBJECT_TYPE               = ClassDesc.of(Object.class.getCanonicalName());
    static String    POST_CONTINUING_EVENT     = "postContinuingEvent";
    static Method    POST_CONTINUING_EVENT_METHOD;
    static String    POST_EVENT                = "postEvent";
    static Method    POST_EVENT_METHOD;
    static String    REMAPPED_TEMPLATE         = "%s$event";
    static String    SHORT_VALUE               = "shortValue";
    static Method    SHORT_VALUE_METHOD;
    static Method    SHORT_VALUE_OF_METHOD;
    static String    SIGNATURE_FOR             = "__signatureFor";
    static Method    SIGNATURE_FOR_METHOD;
    static Method    STRING_BUILDER_CONSTRUCTOR;
    static ClassDesc STRING_BUILDER_TYPE       = ClassDesc.of(StringBuilder.class.getCanonicalName());
    static String    TO_STRING                 = "toString";
    static Method    TO_STRING_METHOD;
    static String    VALUE_OF                  = "valueOf";

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
}
