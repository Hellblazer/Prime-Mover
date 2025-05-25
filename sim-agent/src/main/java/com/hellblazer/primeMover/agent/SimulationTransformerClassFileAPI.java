package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.asm.ClassRemapper;
import com.hellblazer.primeMover.asm.SimulationTransformClassFileAPI;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
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

        var annotationScanner = new AnnotationScanner(Opcodes.ASM9);
        try {
            var cr = new org.objectweb.asm.ClassReader(new ByteArrayInputStream(classfileBuffer));
            cr.accept(annotationScanner, 0);
        } catch (IOException e) {
            log.severe("Unable to read class " + className);
            return null;
        }
        
        if (!annotationScanner.isTransform()) {
            if (annotationScanner.isPreviouslyTransformed()) {
                log.info("Previously transformed " + className);
            }

            // Apply API remapping using ClassFile API
            ClassFile cf = ClassFile.of();
            var classModel = cf.parse(classfileBuffer);
            
            byte[] remappedBytes = cf.build(classModel.thisClass().asSymbol(), 
                classBuilder -> classBuilder.transform(classModel, apiRemapper));
            
            return remappedBytes;
        }

        graph.addClassLoader(loader);

        try (var txfm = new SimulationTransformClassFileAPI(graph)) {
            var generator = txfm.generatorOf(className);
            if (generator == null) {
                return null;
            }

            // Generate the bytecode and apply API remapping to replace Kronos with Kairos
            byte[] generatedBytes = generator.generate();

            // Apply API remapping using ClassFile API
            ClassFile cf = ClassFile.of();
            var classModel = cf.parse(generatedBytes);

            byte[] remappedBytes = cf.build(classModel.thisClass().asSymbol(),
                                            classBuilder -> classBuilder.transform(classModel, apiRemapper));

            log.info("ClassFile API Transformed " + className);
            return remappedBytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to transform", e);
        }
    }
}
