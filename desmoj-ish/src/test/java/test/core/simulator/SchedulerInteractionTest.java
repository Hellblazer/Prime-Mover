package test.core.simulator;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.EventList;
import desmoj.core.simulator.EventNote;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Scheduler;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import test.implementation.TestEntity;
import test.implementation.TestEventAbstract;
import test.implementation.TestExternalEvent;
import test.implementation.TestModel;
import test.implementation.TestSimProcess;
/**
 * This class tests the functionality of the scheduler in interaction with
 * EventList, Model, Entity, Event, SimProcess, EventNote,
 * TimeInstant, TimeSpan and experiment.
 * 
 * @author Sascha Winde
 *
 */
public class SchedulerInteractionTest{

	Scheduler scheduler;
	TestModel model;
	EventList EventList;
	TestEntity entity1;
	TestEntity entity2;
	TestExternalEvent event1;
	TestEventAbstract event2;
	TimeInstant timeInstant;
	TimeSpan timeSpan;
	TestSimProcess process1;
	TestSimProcess process2;
	Experiment experiment;
	EventNote note;
	
	
	@BeforeEach
	public void setUp() throws Exception {
		this.note = new EventNote(entity1, event1, timeInstant);
		this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);
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
	
	/**
	 * First of all it is tested, if the scheduler is connected or not.
	 */
	public void testSchedulerConnected()
	{
		assertNotNull(scheduler);
	}
	
	/**
	 * This Test checks if the test scheduler is connected to the right experiment.
	 */
	public void testExperiment()
	{
		assertEquals(experiment,scheduler.myExperiment);
	}
	
	/**
	 * This Tests checks the current activate Process.
	 */
	public void testSchedulerProcess()
	{
		scheduler.scheduleWithPreempt(process1);
		process1.activatePremempt();
		experiment.start();
		assertEquals(process1, scheduler.getCurrentSimProcess());
		
	}
	
	/**
	 * This test checks, if the scheduler schedules the given ENtitys right.
	 * Therefore, they are scheduled with TimeSpans, activated with TimeInstants or
	 * after one another.
	 */
	public void testScheduleEntity()
	{
		//experiment.stop(new TimeInstant(3));
		System.out.println(scheduler.getCurrentSimProcess());
		scheduler.scheduleWithPreempt(process1);
		process1.activate(new TimeInstant(2));
		scheduler.scheduleAfter(process1, process2, null);
		process2.activate(new TimeInstant(5));
		experiment.start();
		//assertEquals(process1, scheduler.getCurrentSimProcess());
		//experiment.stop(timeInstant);
		//experiment.proceed();
		assertEquals(process2, scheduler.getCurrentSimProcess());
		
	}
	
	/**
	 * This Tests checks if a SImClock is connected and set or not.
	 */
	public void testSimClock()
	{
		assertNotNull(scheduler.getSimClock());
	}

	/**
	 * This Test checks different ways to set the present simulation time.
	 */
	public void testPresentTime()
	{
		experiment.stop(new TimeInstant(6));
		experiment.start();
		System.out.println(scheduler.getSimClock().getTime());
		assertTrue(new TimeInstant(6).getTimeAsDouble() == scheduler.getSimClock().getTime().getTimeAsDouble());	//nicht hingucken, weitergehen ^^
		experiment.stop(timeInstant);
		experiment.proceed();
		System.out.println(scheduler.getSimClock().getTime());
		assertTrue(new TimeInstant(100).getTimeAsDouble() == scheduler.getSimClock().getTime().getTimeAsDouble());	//nicht hingucken, weitergehen ^^
	}
	
	/**
	 * This Tests checks if the current Event is external or not.
	 */
	public void testExternalEvent()
	{
		scheduler.schedule(null, event1, new TimeInstant(4));
		experiment.start();
		assertEquals(event1,scheduler.getCurrentEvent());
	}
	
	/**
	 * This Tests check the randominzingCurrentEvents functionality.
	 */
	public void testRandom()
	{
		scheduler.setRandomizingConcurrentEvents(false);
		assertFalse(scheduler.isRandomizingConcurrentEvents());
		scheduler.setRandomizingConcurrentEvents(true);
		assertTrue(scheduler.isRandomizingConcurrentEvents());
	}
	
	/**
	 * This Test checks the ExecutionSpeedRate
	 */
	public void testSpeed()
	{
		scheduler.setExecutionSpeedRate(10);
		assertTrue(10 == scheduler.getExecutionSpeedRate());
	}
	
	/**
	 * This Tests checks the Eventlist.
	 */
	public void testEventList()
	{
		assertEquals(scheduler.evList, EventList);
	}
	
	@AfterEach
	public void tearDown() throws Exception {
	}

}
