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
package com.hellblazer.primeMover.agent;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

/**
 * Java agent for runtime transformation of Prime Mover simulation classes.
 * <p>
 * This agent can be used in two ways:
 * <ul>
 *   <li><b>premain</b>: Attach at JVM startup via {@code -javaagent:sim-agent.jar}</li>
 *   <li><b>agentmain</b>: Attach dynamically to a running JVM</li>
 * </ul>
 * <p>
 * The agent performs two types of transformations:
 * <ol>
 *   <li>Entity classes ({@code @Entity} annotated) are fully transformed to support
 *       discrete event simulation with method calls becoming events</li>
 *   <li>Classes using the {@code Kronos} API have references remapped to {@code Kairos}
 *       for runtime controller access</li>
 * </ol>
 * <p>
 * Usage:
 * <pre>
 * java -javaagent:sim-agent.jar -jar myapp.jar
 * </pre>
 *
 * @author hal.hildebrand
 * @see SimulationTransformerClassFileAPI
 */
public class SimAgent {
    private static final Logger log = Logger.getLogger(SimAgent.class.getName());

    /**
     * Entry point for dynamic agent attachment.
     *
     * @param agentArgs arguments passed to the agent (currently unused)
     * @param inst      the instrumentation instance for class transformation
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        log.info("[SimAgent] Dynamic attach - installing transformer");
        installTransformer(inst);
    }

    /**
     * Entry point for JVM startup agent attachment.
     *
     * @param agentArgs arguments passed to the agent (currently unused)
     * @param inst      the instrumentation instance for class transformation
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        log.info("[SimAgent] Premain - installing transformer");
        installTransformer(inst);
    }

    private static void installTransformer(Instrumentation inst) {
        var transformer = new SimulationTransformerClassFileAPI();
        inst.addTransformer(transformer, true);
        log.info("[SimAgent] Transformer installed successfully");
    }
}
