package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.classfile.ClassRemapper;
import com.hellblazer.primeMover.classfile.SimulationTransform;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * ClassFile API version of SimulationTransformer for runtime transformation
 *
 * @author hal.hildebrand
 **/

public class SimulationTransformerClassFileAPI implements ClassFileTransformer {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(
    SimulationTransformerClassFileAPI.class.getCanonicalName());

    private final ClassRemapper apiRemapper;
    private       ClassGraph    graph = new ClassGraph();

    public SimulationTransformerClassFileAPI() {
        // Create mapping function for ClassFile API ClassRemapper
        apiRemapper = new ClassRemapper(classDesc -> {
            String className = classDesc.packageName() + "." + classDesc.displayName();
            if (className.equals(Kronos.class.getCanonicalName())) {
                return ClassDesc.of(Kairos.class.getCanonicalName());
            }
            return classDesc;
        });
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (loader == null) {
            return null;
        }

        // Parse class and scan for annotations using ClassFile API
        ClassFile cf = ClassFile.of();
        var classModel = cf.parse(classfileBuffer);
        var annotationScanner = new AnnotationScanner();
        annotationScanner.scan(classModel);

        if (!annotationScanner.isTransform()) {
            if (annotationScanner.isPreviouslyTransformed()) {
                log.info("Previously transformed " + className);
            }

            // Apply API remapping using ClassFile API
            var remappedBytes = cf.build(classModel.thisClass().asSymbol(),
                                         classBuilder -> classBuilder.transform(classModel, apiRemapper));

            return remappedBytes;
        }

        graph.addClassLoader(loader);

        try (var txfm = new SimulationTransform(graph)) {
            var generator = txfm.generatorOf(className);
            if (generator == null) {
                return null;
            }

            // Generate the bytecode and apply API remapping to replace Kronos with Kairos
            var generatedBytes = generator.generate();

            // Apply API remapping using ClassFile API
            var generatedClassModel = cf.parse(generatedBytes);

            var remappedBytes = cf.build(generatedClassModel.thisClass().asSymbol(),
                                         classBuilder -> classBuilder.transform(generatedClassModel, apiRemapper));

            log.info("ClassFile API Transformed " + className);
            return remappedBytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to transform", e);
        }
    }
}
