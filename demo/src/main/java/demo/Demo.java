/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package demo;

import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.api.SimulationException;
import com.hellblazer.primeMover.builders.SimulationBuilder;
import com.hellblazer.primeMover.controllers.SimulationController;

import java.util.Map;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class Demo {

    public static void channel() throws SimulationException {
        // New builder pattern reduces boilerplate
        var controller = (SimulationController) SimulationBuilder.builder()
                                                                 .trackSpectrum(true)
                                                                 .build();
        new UseChannel().test();
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : " + spectrumEntry.getKey());
        }
    }

    public static void channelOldWay() throws SimulationException {
        // Old way still works for backward compatibility
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        new UseChannel().test();
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : " + spectrumEntry.getKey());
        }
    }

    public static void eventContinuationThroughput() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        new Driver().runContinuationBenchmark("STRING", 100000, 10);
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : " + spectrumEntry.getKey());
        }
    }

    public static void eventThroughput() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        new Driver().runEventBenchmark("STRING", 100000, 100);
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : " + spectrumEntry.getKey());
        }
    }

    public static void main(String[] argv) throws Exception {
        try {
            eventContinuationThroughput();
            System.out.println();
            System.out.println();
            eventThroughput();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void threaded() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : " + spectrumEntry.getKey());
        }
    }
}
