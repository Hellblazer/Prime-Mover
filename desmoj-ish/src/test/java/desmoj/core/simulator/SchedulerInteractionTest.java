package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.implementation.TestEntity;
import desmoj.implementation.TestEventAbstract;
import desmoj.implementation.TestExternalEvent;
import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;

/**
 * This class tests the functionality of the scheduler in interaction with
 * EventList, Model, Entity, Event, SimProcess, EventNote, TimeInstant, TimeSpan
 * and experiment.
 *
 * @author Sascha Winde
 *
 */
public class SchedulerInteractionTest {

    TestEntity        entity1;
    TestEntity        entity2;
    TestExternalEvent event1;
    TestEventAbstract event2;
    EventList         EventList;
    Experiment        experiment;
    TestModel         model;
    TestSimProcess    process1;
    TestSimProcess    process2;
    Scheduler         scheduler;
    TimeInstant       timeInstant;
    TimeSpan          timeSpan;

    @BeforeEach
    public void setUp() throws Exception {
        this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                         java.util.concurrent.TimeUnit.HOURS, null);
        this.model = new TestModel();
        model.connectToExperiment(experiment);
        this.scheduler = experiment.getScheduler();
        scheduler.myExperiment = experiment;
        this.EventList = scheduler.evList;
        this.entity1 = new TestEntity(model, "First Test Entity", false);
        this.entity2 = new TestEntity(model, "Second Test Entity", false);
        this.event1 = new TestExternalEvent(model, "First Test Event", false);
        this.event2 = new TestEventAbstract(model, "Second Test Event", false);
        this.timeInstant = new TimeInstant(100);
        this.timeSpan = new TimeSpan(5);
        this.process1 = new TestSimProcess(model, "First Test SimProcess", false);
        this.process2 = new TestSimProcess(model, "Second Test SimProcess", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Tests checks the Eventlist.
     */
    @Test
    public void testEventList() {
        assertEquals(scheduler.evList, EventList);
    }

    /**
     * This Test checks if the test scheduler is connected to the right experiment.
     */
    @Test
    public void testExperiment() {
        assertEquals(experiment, scheduler.myExperiment);
    }

    /**
     * This Tests checks if the current Event is external or not.
     */
    @Test
    public void testExternalEvent() {
        scheduler.scheduleNoPreempt(null, event1, new TimeInstant(4));
        experiment.start();
        assertEquals(event1, scheduler.getCurrentEvent());
    }

    /**
     * This Test checks different ways to set the present simulation time.
     */
    @Test
    public void testPresentTime() {
        experiment.stop(new TimeInstant(6));
        experiment.start();
        System.out.println(scheduler.getSimClock().getTime());
        assertTrue(new TimeInstant(6).getTimeAsDouble() == scheduler.getSimClock().getTime().getTimeAsDouble()); // nicht
                                                                                                                 // hingucken,
                                                                                                                 // weitergehen
                                                                                                                 // ^^
        experiment.stop(timeInstant);
        experiment.proceed();
        System.out.println(scheduler.getSimClock().getTime());
        assertTrue(new TimeInstant(100).getTimeAsDouble() == scheduler.getSimClock().getTime().getTimeAsDouble()); // nicht
                                                                                                                   // hingucken,
                                                                                                                   // weitergehen
                                                                                                                   // ^^
    }

    /**
     * This Tests check the randominzingCurrentEvents functionality.
     */
    @Test
    public void testRandom() {
//        EventList.setRandomizingConcurrentEvents(false);
//        assertFalse(scheduler.isRandomizingConcurrentEvents());
//        EventList.setRandomizingConcurrentEvents(true);
//        assertTrue(scheduler.isRandomizingConcurrentEvents());
    }

    /**
     * This test checks, if the scheduler schedules the given ENtitys right.
     * Therefore, they are scheduled with TimeSpans, activated with TimeInstants or
     * after one another.
     */
    @Test
    public void testScheduleEntity() {
        // experiment.stop(new TimeInstant(3));
        System.out.println(scheduler.getCurrentSimProcess());
        scheduler.scheduleWithPreempt(process1, null);
        process1.activate(new TimeInstant(2));
        scheduler.scheduleAfter(process1, process2, null);
        process2.activate(new TimeInstant(5));
        experiment.start();
        // assertEquals(process1, scheduler.getCurrentSimProcess());
        // experiment.stop(timeInstant);
        // experiment.proceed();
        assertEquals(process2, scheduler.getCurrentSimProcess());

    }

    /**
     * First of all it is tested, if the scheduler is connected or not.
     */
    @Test
    public void testSchedulerConnected() {
        assertNotNull(scheduler);
    }

    /**
     * This Tests checks the current activate Process.
     */
    @Test
    public void testSchedulerProcess() {
        scheduler.scheduleWithPreempt(process1, null);
        process1.activatePreempt();
        experiment.start();
        assertEquals(process1, scheduler.getCurrentSimProcess());

    }

    /**
     * This Tests checks if a SImClock is connected and set or not.
     */
    @Test
    public void testSimClock() {
        assertNotNull(scheduler.getSimClock());
    }

    /**
     * This Test checks the ExecutionSpeedRate
     */
    @Test
    public void testSpeed() {
        scheduler.setExecutionSpeedRate(10);
        assertTrue(10 == scheduler.getExecutionSpeedRate());
    }

}
