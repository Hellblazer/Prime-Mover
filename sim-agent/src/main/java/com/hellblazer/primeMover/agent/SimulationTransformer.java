package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.asm.SimulationTransform;
import io.github.classgraph.ClassGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author hal.hildebrand
 **/

public class SimulationTransformer implements ClassFileTransformer {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(
    SimulationTransformer.class.getCanonicalName());

    private ClassGraph graph = new ClassGraph();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (loader == null) {
            return null;
        }

        ClassReader cr = null;
        try {
            cr = new ClassReader(new ByteArrayInputStream(classfileBuffer));
        } catch (IOException e) {
            log.severe("Unable to read class " + className);
        }
        var as = new AnnotationScanner(Opcodes.ASM9);
        cr.accept(as, 0);
        if (!as.isTransform()) {
            if (as.isPreviouslyTransformed()) {
                log.info("Previously transformed " + className);
            }
            return null;
        }

        graph.addClassLoader(loader);

        try (var txfm = new SimulationTransform(graph)) {
            var generator = txfm.generatorOf(className);
            log.info("Transformed " + className);
            return generator == null ? null : generator.generate().toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to transform", e);
        }
    }
}
