/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.soot;

import static com.hellblazer.primeMover.soot.util.Utils.isEntity;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import soot.Hack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.baf.Baf;
import soot.jimple.JimpleBody;
import soot.options.Options;

/**
 * The class that sets up the entire chain of transformations for the Prime
 * Mover event driven simulation framework.
 * <p>
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class SimulationTransform {
    private static Logger log = Logger.getLogger(SimulationTransform.class.getCanonicalName());

    public static String getBootClasspath() {
        String cp = System.getProperty("sun.boot.library.path");
        if (cp != null) {
            log.info(String.format("Using[1] boot library path: %s", cp));
            return cp;
        }
        cp = System.getProperty("java.boot.class.path");
        if (cp != null) {
            log.info(String.format("Using[2] boot class path: %s", cp));
            return cp;
        }
        Enumeration<?> i = System.getProperties().propertyNames();
        String name = null;
        while (i.hasMoreElements()) {
            String temp = (String) i.nextElement();
            if (temp.indexOf(".boot.class.path") != -1) {
                if (name == null) {
                    name = temp;
                } else {
                    throw new IllegalStateException("Cannot auto-detect boot class path "
                    + System.getProperty("java.version"));
                }
            }
        }
        if (name == null) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.1.")) {
                // by default, the current directory is added to the classpath
                // we therefore need to strip that out
                cp = System.getProperty("java.class.path");
                cp = removeAll(cp, ".");
                cp = removeAll(cp, new File(".").getAbsolutePath());
                try {
                    cp = removeAll(cp, new File(".").getCanonicalPath());
                } catch (IOException e) {
                    // ignore
                }
                cp = removeAll(cp, new File(".").getAbsolutePath() + System.getProperty("file.separator"));
                try {
                    cp = removeAll(cp, new File(".").getCanonicalPath() + System.getProperty("file.separator"));
                } catch (IOException e) {
                    // ignore
                }
                log.info(String.format("Using[3] boot class path: %s", cp));
                return cp;
            }
            log.severe("Cannot auto-detect boot class path " + System.getProperty("java.version") + " "
            + System.getProperty("java.class.path"));
            throw new IllegalStateException("Cannot auto-detect boot class path ");
        }
        log.info(String.format("Using[4] boot class path: %s", System.getProperty(name)));
        return System.getProperty(name);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toUpperCase().startsWith("WINDOWS");
    }

    public static void main(String[] args) {
        try {
            new SimulationTransform().execute(args);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static String removeAll(String cp, String prefix) {
        String pathSeparator = System.getProperty("path.separator");
        if (cp.startsWith(prefix + pathSeparator)) {
            cp = cp.substring(prefix.length() + pathSeparator.length());
        }
        int j;
        while (-1 != (j = cp.indexOf(pathSeparator + prefix + pathSeparator))) {
            cp = cp.substring(0, j) + cp.substring(j + prefix.length() + pathSeparator.length());
        }
        if (cp.endsWith(pathSeparator + prefix)) {
            cp = cp.substring(0, cp.length() - prefix.length() + pathSeparator.length());
        }
        if (isWindows()) {
            // we might have the prefix or the classpath case differing
            if (cp.toUpperCase().startsWith((prefix + pathSeparator).toUpperCase())) {
                cp = cp.substring(prefix.length() + pathSeparator.length());
            }
            while (-1 != (j = cp.toUpperCase().indexOf((pathSeparator + prefix + pathSeparator).toUpperCase()))) {
                cp = cp.substring(0, j) + cp.substring(j + prefix.length() + pathSeparator.length());
            }
            if (cp.toUpperCase().endsWith((pathSeparator + prefix).toUpperCase())) {
                cp = cp.substring(0, cp.length() - prefix.length() + pathSeparator.length());
            }
        }
        return cp;
    }

    public static void setStandardClassPath() {
        final var mp = System.getProperty("jdk.module.path");
        Options.v().set_soot_modulepath(mp == null ? "" : mp);
        Options.v().set_process_dir(Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)));
    }

    private String generatedDirectory;

    public void execute(String[] args) {
        if (args == null) {
            args = new String[] {};
        }
        Options.v().set_unfriendly_mode(true);
        final List<SootClass> generated = new ArrayList<SootClass>();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.continuation.analysis", new ContinuationAnalysis()));
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.entity.generation", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
                Iterator<SootClass> applicationClasses = Scene.v().getApplicationClasses().snapshotIterator();
                while (applicationClasses.hasNext()) {
                    SootClass applicationClass = applicationClasses.next();
                    if (isEntity(applicationClass)) {
                        new EntityGenerator(generated, applicationClass).generateEntity();
                    }
                }

            }
        }));
        PackManager.v().getPack("jtp").add(new Transform("jtp.api.transform", new ApiTransformer()));
        PackManager.v()
                   .getPack("jtp")
                   .add(new Transform("jtp.continuation.transform", new ContinuationTransformer(generated)));
        PackManager.v()
                   .getPack("jtp")
                   .add(new Transform("jtp.entity.creation.transform", new EntityConstructionTransformer()));
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("tag.ln", "on");

        Options.v()
               .set_exclude(asList("com.hellblazer.primeMover", "com.hellblazer.primeMover.controllers",
                                   "com.hellblazer.primeMover.runtime", "com.hellblazer.primeMover.soot"));
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_whole_program(true);
        // Options.v().set_app(true);

        soot.Main.main(args);

        if (generatedDirectory != null) {
            Options.v().set_output_dir(generatedDirectory);
        }

        for (SootClass generatedClass : generated) {
            for (SootMethod method : generatedClass.getMethods()) {
                method.setActiveBody(Baf.v().newBody((JimpleBody) method.getActiveBody()));
            }
            Hack.writeClass(generatedClass, PackManager.v());
        }
    }

    public String getGeneratedDirectory() {
        return generatedDirectory;
    }

    public void setGeneratedDirectory(String generatedDirectory) {
        this.generatedDirectory = generatedDirectory;
    }

}
