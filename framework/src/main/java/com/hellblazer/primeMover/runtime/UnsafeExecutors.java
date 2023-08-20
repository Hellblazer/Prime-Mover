package com.hellblazer.primeMover.runtime;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public class UnsafeExecutors {
    private static class BTB {
        private int                      characteristics;
        private long                     counter;
        private String                   name;
        private UncaughtExceptionHandler uhe;
    }

    private static class VirtualThreadExecutor implements Executor {
        private final Executor executor;

        public VirtualThreadExecutor(Executor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable command) {
            var builder = Thread.ofVirtual();
            setExecutor(builder, executor);
            builder.start(command);
        }
    }

    private static class VTB extends BTB {
        private Executor executor;
    }

    private static final MethodHandle SET_EXECUTOR;

    public static <B extends Thread.Builder> B configureBuilderExecutor(B builder, Executor executor) {
        if (executor != null) {
            setExecutor(builder, executor);
        }
        return builder;
    }

    public static Executor virtualThreadExecutor(Executor executor) {
        Objects.requireNonNull(executor);
        return new VirtualThreadExecutor(executor);
    }

    private static void setExecutor(Object builder, Object executor) {
        try {
            SET_EXECUTOR.invokeExact(builder, executor);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static {
        try {
            var unsafeClass = Class.forName("sun.misc.Unsafe");
            var unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            var unsafe = unsafeField.get(null);
            var objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
            var executorField = VTB.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            var executorOffset = (long) objectFieldOffset.invoke(unsafe, executorField);
            var putObject = MethodHandles.lookup()
                                         .findVirtual(unsafeClass, "putObject",
                                                      methodType(void.class, Object.class, long.class, Object.class));
            var setExecutor = insertArguments(insertArguments(putObject, 2, executorOffset), 0, unsafe);
            SET_EXECUTOR = setExecutor;
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException
        | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
