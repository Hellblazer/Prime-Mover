/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.primeMover.asm;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * @author hal.hildebrand
 */
public class SimulationTransformTest {

    @Test
    public void smokin() throws Exception {
        String pkg = "com.hellblazer.primeMover";
        String routeAnnotation = pkg + ".Entity";
        try (ScanResult scanResult = new ClassGraph().verbose() // Log to stderr
                                                     .enableAllInfo() // Scan classes, methods, fields, annotations
                                                     .acceptPackages() // Scan com.xyz and subpackages
                                                                       // (omit to scan
                                                     // all packages)
                                                     .scan()) { // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(routeAnnotation)) {
                AnnotationInfo routeAnnotationInfo = routeClassInfo.getAnnotationInfo(routeAnnotation);
                List<AnnotationParameterValue> routeParamVals = routeAnnotationInfo.getParameterValues();
                // @com.xyz.Route has one required parameter
                final var value = routeParamVals.get(0).getValue();
                var iFaces = (Object[]) value;
                System.out.println(routeClassInfo.getName() + " is annotated with " + iFaces[0]);
            }
        }
    }
}
