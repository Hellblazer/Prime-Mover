package test;

import static demo.Demo.channel;
import static demo.Demo.eventContinuationThroughput;
import static demo.Demo.eventThroughput;
import static demo.Demo.threaded;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.controllers.SimulationController;

import hello.HelloWorld;

public class Me {

    @Test
    public void helloWorld() throws Exception {
        SimulationController controller = new SimulationController();
        controller.setEndTime(200);
        Kronos.setController(controller);
        new HelloWorld().event1();
        controller.eventLoop();
    }

    @Test
    public void runDemo() throws Exception {
        threaded();
        System.out.println();
        System.out.println();
        channel();
        System.out.println();
        System.out.println();
        eventContinuationThroughput();
        System.out.println();
        System.out.println();
        eventThroughput();
    }
}
