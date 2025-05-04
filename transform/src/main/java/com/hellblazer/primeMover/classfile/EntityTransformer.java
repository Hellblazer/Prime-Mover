package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.asm.OpenAddressingSet;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.MethodInfo;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.classfile.ClassTransform.transformingMethodBodies;
import static java.lang.constant.ConstantDescs.*;

/**
 * @author hal.hildebrand
 **/
public class EntityTransformer {

    private final Set<Method>              blocking;
    private final Set<Method>              events;
    private final Map<Method, Integer>     inverse;
    private final Map<Integer, MethodInfo> mapped;
    private final Set<MethodInfo>          remapped;
    private final ClassDesc                type;

    public EntityTransformer(ClassDesc clazz, Set<MethodInfo> events) {
        type = clazz;
        mapped = new HashMap<>();
        remapped = new OpenAddressingSet.OpenSet<>();
        blocking = new OpenAddressingSet.OpenSet<>();
        inverse = new HashMap<>();
        this.events = new OpenAddressingSet.OpenSet<>();
        var key = 0;
        for (var mi : events.stream().sorted().toList()) {
            mapped.put(key, mi);
            final var event = new Method(ClassDesc.of(mi.getClassName()), mi.getName(),
                                         MethodTypeDesc.ofDescriptor(mi.getTypeDescriptorStr()));
            inverse.put(event, key++);
            this.events.add(event);
            if (!mi.getTypeDescriptor().getResultType().toString().equals("void") || mi.hasAnnotation(Blocking.class)) {
                blocking.add(event);
            }
//            final boolean declared = clazz.getDeclaredMethodInfo(mi.getName()).stream().anyMatch(m -> mi.equals(m));
//            if (declared) {
//                remapped.add(mi);
//            }
        }
    }

    public byte[] transform(byte[] bytes) {
        var classModel = ClassFile.of().parse(bytes);
        for (var classElement : classModel) {
            switch (classElement) {
                case MethodModel mm -> System.out.printf("Method name %s type %s%n", mm.methodName().stringValue(),
                                                         mm.methodType().stringValue());
                default -> {
                }
            }
        }
        CodeTransform apiTransform = (codeBuilder, e) -> {
            switch (e) {
                case InvokeInstruction i -> {
                    var method = new Method(i.owner().asSymbol(), i.name().stringValue(), i.typeSymbol());
                    if (!mapped.containsKey(method)) {
                        codeBuilder.invoke(i.opcode(), method.defining, i.name().stringValue(), i.typeSymbol(),
                                           i.isInterface());
                    } else {
                        codeBuilder.accept(e);
                    }
                }
                default -> codeBuilder.accept(e);
            }
        };

        var classMap = Map.of(ClassDesc.of(Kronos.class.getCanonicalName()),
                              ClassDesc.of(Kairos.class.getCanonicalName()));
        var classRemapper = new ClassRemapper(desc -> classMap.getOrDefault(desc, desc));
        var classTransform = transformingMethodBodies(apiTransform).andThen(classRemapper);
        return ClassFile.of().transformClass(classModel, classTransform);
    }

    private void boxIt(CodeBuilder builder, ClassDesc type) {
        if (type == CD_byte) {
            builder.invokestatic(Constants.BYTE_VALUE_OF_METHOD.defining, Constants.BYTE_VALUE_OF_METHOD.name,
                                 Constants.BYTE_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_char) {
            builder.invokestatic(Constants.CHARACTER_VALUE_OF_METHOD.defining, Constants.CHARACTER_VALUE_OF_METHOD.name,
                                 Constants.CHARACTER_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_double) {
            builder.invokestatic(Constants.DOUBLE_VALUE_OF_METHOD.defining, Constants.DOUBLE_VALUE_OF_METHOD.name,
                                 Constants.DOUBLE_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_float) {
            builder.invokestatic(Constants.FLOAT_VALUE_OF_METHOD.defining, Constants.FLOAT_VALUE_OF_METHOD.name,
                                 Constants.FLOAT_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_int) {
            builder.invokestatic(Constants.INTEGER_VALUE_OF_METHOD.defining, Constants.INTEGER_VALUE_OF_METHOD.name,
                                 Constants.INTEGER_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_long) {
            builder.invokestatic(Constants.LONG_VALUE_OF_METHOD.defining, Constants.LONG_VALUE_OF_METHOD.name,
                                 Constants.LONG_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_short) {
            builder.invokestatic(Constants.SHORT_VALUE_OF_METHOD.defining, Constants.SHORT_VALUE_OF_METHOD.name,
                                 Constants.SHORT_VALUE_OF_METHOD.descriptor);
            return;
        }
        if (type == CD_boolean) {
            builder.invokestatic(Constants.BOOLEAN_VALUE_OF_METHOD.defining, Constants.BOOLEAN_VALUE_OF_METHOD.name,
                                 Constants.BOOLEAN_VALUE_OF_METHOD.descriptor);
            return;
        }
        throw new IllegalArgumentException("Unknown parameter type: " + type);
    }

    public record Method(ClassDesc defining, String name, MethodTypeDesc descriptor) {
        static Method getMethod(final java.lang.reflect.Method method) {
            return new Method(ClassDesc.of(method.getDeclaringClass().getCanonicalName()), method.getName(),
                              MethodTypeDesc.of(ClassDesc.of(method.getReturnType().getCanonicalName()), Arrays.stream(
                              method.getParameterTypes()).map(e -> ClassDesc.of(e.getCanonicalName())).toList()));
        }

        public static Method getMethod(Constructor<?> constructor) {
            return new Method(ClassDesc.of(constructor.getDeclaringClass().getCanonicalName()), "<init>",
                              MethodTypeDesc.of(ClassDesc.of(constructor.getDeclaringClass().getCanonicalName()),
                                                Arrays.stream(constructor.getParameterTypes())
                                                      .map(e -> ClassDesc.of(e.getCanonicalName()))
                                                      .toList()));
        }
    }
}
