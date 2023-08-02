package desmoj.core.simulator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.implementation.TestEventAbstract;
import desmoj.implementation.TestModel;


/**
 * This class tests the methods derived of the abstract
 * class Event. 
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public abstract class EventModulTest{

	TestModel model;
	TestEventAbstract event;
	
	/**
	 * Sets up the test fixture.
	 */
	@BeforeEach
	public void setUp() throws Exception {
		this.model = new TestModel();
		Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);
		model.connectToExperiment(experiment);
		
		this.event = new TestEventAbstract(model, "First Test Event", false);
	}

	
	/**
	 * This test checks the given name.
	 */
	public void testName()
	{
		assertEquals("First Test Event#1", event.getName());
	}
	
	/**
	 * This Test checks the actual RealTimeConstraint
	 */
	public void testRealTimeConstraint()
	{
		assertTrue(10 == event.getRealTimeConstraint());
	}
	
	/**
	 * This Test checks, if an event is not scheduled.
	 */
	public void testIsNotScheduled()
	{
		assertFalse(event.isScheduled());
	}
	
	
	@AfterEach
	public void tearDown() throws Exception {
	}

}
