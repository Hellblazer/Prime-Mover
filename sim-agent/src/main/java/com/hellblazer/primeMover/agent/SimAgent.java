package com.hellblazer.primeMover.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author hal.hildebrand
 **/
public class SimAgent {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(
    SimAgent.class.getName());

    public static void agentmain(String agentArgs, Instrumentation inst) {
        log.info("[Agent] In agentmain method using ClassFile API");
        inst.addTransformer(new SimulationTransformerClassFileAPI(), true);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        log.info("[Agent] In premain method using ClassFile API");
        inst.addTransformer(new SimulationTransformerClassFileAPI(), true);
    }
}
