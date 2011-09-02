package com.hellblazer.primeMover.soot;

import static com.hellblazer.primeMover.soot.Util.OUTPUT_DIR;
import static com.hellblazer.primeMover.soot.Util.PROCESSED_DIR;
import static com.hellblazer.primeMover.soot.Util.SOURCE_DIR;
import static com.hellblazer.utils.Utils.copy;
import static com.hellblazer.utils.Utils.getBits;
import static com.hellblazer.utils.Utils.initializeDirectory;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import junit.framework.TestCase;
import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.options.Options;

import com.hellblazer.primeMover.Continuable;

public class TestContinuationAnalysis extends TestCase {
    public void testAnalysis() throws Exception {
        G.reset();
        initializeDirectory(OUTPUT_DIR);
        populateProcessedDir();

        // Options.v().setPhaseOption("cg", "verbose:true");
        PackManager.v().getPack("wjtp").add(new Transform(
                                                          "wjtp.continuation.analysis",
                                                          new ContinuationAnalysis()));
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("tag.ln", "on");

        Options.v().set_whole_program(true);
        // Options.v().set_verbose(true);
        // Options.v().set_app(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_process_dir(asList(PROCESSED_DIR.getAbsolutePath()));
        Options.v().set_output_dir(OUTPUT_DIR.getAbsolutePath());

        String[] argv = { "-w", "-process-dir",
                         PROCESSED_DIR.getAbsolutePath(), "-v" };
        argv = new String[] { "-w" };
        // argv = new String[] { "--help" }; 
        soot.Main.main(argv);

        LocalLoader loader = new LocalLoader(getOutputBits());

        Class<?> clazz = loader.loadClass(InferredContinuation.class.getCanonicalName());
        assertNotSame(InferredContinuation.class, clazz);
        Method method = clazz.getMethod("a");
        assertNotNull(method);
        Continuable annotation = method.getAnnotation(Continuable.class);
        assertNotNull(annotation);

        method = clazz.getMethod("b");
        assertNotNull(method);
        annotation = method.getAnnotation(Continuable.class);
        assertNotNull(annotation);
    }

    private HashMap<String, byte[]> getOutputBits() throws IOException {
        HashMap<String, byte[]> classBits = new HashMap<String, byte[]>();
        String className = InferredContinuation.class.getCanonicalName();
        classBits.put(className,
                      getBits(new File(OUTPUT_DIR, className.replace('.', '/')
                                                   + ".class")));
        return classBits;
    }

    private void populateProcessedDir() throws IOException {
        String pkg = InferredContinuation.class.getPackage().getName().replace('.',
                                                                               '/');
        File targetDir = new File(PROCESSED_DIR, pkg);
        File sourceDir = new File(SOURCE_DIR, pkg);
        initializeDirectory(targetDir);

        for (String clazz : new String[] {
                                          ContinuationPrototype.class.getSimpleName(),
                                          InferredContinuation.class.getSimpleName() }) {
            String classFileName = clazz + ".class";
            copy(new File(sourceDir, classFileName), new File(targetDir,
                                                              classFileName));
        }
    }
}
