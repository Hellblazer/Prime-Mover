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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.baf.Baf;
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
    public static void main(String[] args) {
        new SimulationTransform().execute(args);
    }

    private String generatedDirectory;

    public void execute(String[] args) {
        if (args == null) {
            args = new String[] {};
        }
        Options.v().set_unfriendly_mode(true);
        final List<SootClass> generated = new ArrayList<SootClass>();

        PackManager.v().getPack("wjtp").add(new Transform(
                                                          "wjtp.continuation.analysis",
                                                          new ContinuationAnalysis()));
        PackManager.v().getPack("wjtp").add(new Transform(
                                                          "wjtp.entity.generation",
                                                          new SceneTransformer() {

                                                              @Override
                                                              protected void internalTransform(String phaseName,
                                                                                               @SuppressWarnings("rawtypes") Map options) {
                                                                  Iterator<SootClass> applicationClasses = Scene.v().getApplicationClasses().snapshotIterator();
                                                                  while (applicationClasses.hasNext()) {
                                                                      SootClass applicationClass = applicationClasses.next();
                                                                      if (isEntity(applicationClass)) {
                                                                          new EntityGenerator(
                                                                                              generated,
                                                                                              applicationClass).generateEntity();
                                                                      }
                                                                  }

                                                              }
                                                          }));
        PackManager.v().getPack("jtp").add(new Transform("jtp.api.transform",
                                                         new ApiTransformer()));
        PackManager.v().getPack("jtp").add(new Transform(
                                                         "jtp.continuation.transform",
                                                         new ContinuationTransformer(
                                                                                     generated)));
        PackManager.v().getPack("jtp").add(new Transform(
                                                         "jtp.entity.creation.transform",
                                                         new EntityConstructionTransformer()));
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("tag.ln", "on");

        Options.v().set_exclude(asList("com.hellblazer.primeMover",
                                       "com.hellblazer.primeMover.controllers",
                                       "com.hellblazer.primeMover.runtime",
                                       "com.hellblazer.primeMover.soot"));
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_whole_program(true);
        // Options.v().set_app(true);

        soot.Main.main(args);

        if (generatedDirectory != null) {
            Options.v().set_output_dir(generatedDirectory);
        }

        for (SootClass generatedClass : generated) {
            for (SootMethod method : generatedClass.getMethods()) {
                method.setActiveBody(Baf.v().newBody(method.getActiveBody()));
            }
            PackManager.v().writeClass(generatedClass);
        }
    }

    public String getGeneratedDirectory() {
        return generatedDirectory;
    }

    public void setGeneratedDirectory(String generatedDirectory) {
        this.generatedDirectory = generatedDirectory;
    }
}
