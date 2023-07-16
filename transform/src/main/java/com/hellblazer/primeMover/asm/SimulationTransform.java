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

import com.hellblazer.primeMover.Blocking;
import com.hellblazer.primeMover.Entity;
import com.hellblazer.primeMover.Event;
import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.NonEvent;
import com.hellblazer.primeMover.runtime.Kairos;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * @author hal.hildebrand
 */
public class SimulationTransform {
    private final ClassInfo  blockingAnnotation;
    private final ClassInfo  entityAnnotation;
    private final ClassInfo  eventAnnotation;
    private final ClassInfo  kairos;
    private final ClassInfo  kronos;
    private final ClassInfo  nonEventAnnotation;
    private final ScanResult scan;

    public SimulationTransform(ClassGraph graph) {
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses();
        scan = graph.scan();
        entityAnnotation = scan.getClassInfo(Entity.class.getCanonicalName());
        blockingAnnotation = scan.getClassInfo(Blocking.class.getCanonicalName());
        eventAnnotation = scan.getClassInfo(Event.class.getCanonicalName());
        nonEventAnnotation = scan.getClassInfo(NonEvent.class.getCanonicalName());
        kairos = scan.getClassInfo(Kairos.class.getCanonicalName());
        kronos = scan.getClassInfo(Kronos.class.getCanonicalName());
    }

    public ClassInfoList findAllEntities() {
        var allEntities = new ClassInfoList();

        final var explicitEntities = scan.getClassesWithAnnotation(Entity.class.getCanonicalName());
        allEntities = allEntities.union(explicitEntities);
        for (var ci : explicitEntities) {
            allEntities = allEntities.union(ci.getSubclasses());
        }
        return allEntities;
    }

}
