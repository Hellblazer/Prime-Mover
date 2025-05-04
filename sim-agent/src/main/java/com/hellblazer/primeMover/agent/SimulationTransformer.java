package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.asm.SimulationTransform;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;

/**
 * @author hal.hildebrand
 **/

public class SimulationTransformer implements ClassFileTransformer {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(
    SimulationTransformer.class.getCanonicalName());

    private final SimpleRemapper apiRemapper;
    private       ClassGraph     graph = new ClassGraph();

    public SimulationTransformer() {
        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), Kairos.class.getCanonicalName().replace('.', '/'));
        apiRemapper = new SimpleRemapper(map);
    }

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

            try {
                cr = new ClassReader(new ByteArrayInputStream(classfileBuffer));
            } catch (IOException e) {
                log.severe("Unable to read class " + className);
            }
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cr.accept(new ClassRemapper(cw, apiRemapper), ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
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
