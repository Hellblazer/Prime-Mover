/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.TrackingController;
import com.hellblazer.primeMover.runtime.Framework;

import io.github.classgraph.ClassGraph;
import testClasses.LocalLoader;

/**
 * Test the end to end behavior of simulation transform.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class TestEndToEnd {

    @Test
    public void testTransform() throws Exception {
        ClassLoader loader = new LocalLoader(getTransformed());
        TrackingController controller = new TrackingController();
        // controller.setDebugEvents(true);
        // controller.setEventLogger(Logger.getLogger("Event Logger"));
        Framework.setController(controller);
        Class<?> entity1Clazz = loader.loadClass("testClasses.Entity1Impl");

        Object entity;
        entity = entity1Clazz.getConstructor().newInstance();
        Method event = entity1Clazz.getMethod("event1");
        controller.setCurrentTime(0);
        event.invoke(entity);

        while (controller.send()) {

        }

        assertEquals(6, controller.events.size());
        assertEquals(2, controller.blockingEvents.size());
        assertEquals(8, controller.references.size());

        int i = 0;

        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event2(testClasses.Entity2)>", controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
        assertEquals("<testClasses.Entity2Impl: void myEvent()>", controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
    }

    private Map<String, byte[]> getTransformed() throws Exception {
        try (var transform = new SimulationTransform(new ClassGraph().acceptPackages("testClasses",
                                                                                     "com.hellblazer.*"))) {
            return transform.generators()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey().getName().replace('.', '/'), e -> {
                                try {
                                    final var bytes = e.getValue().generate().toByteArray();
//                                    dump(bytes);
                                    return bytes;
                                } catch (IOException e1) {
                                    throw new IllegalStateException(e1);
                                }
                            }));
        }
    }
}
