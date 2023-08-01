package test.core.simulator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import test.implementation.TestExternalEvent;
import test.implementation.TestModel;

/**
 * This Class simply tests if an Event is external or not.
 * Further functionality is tested in the EventModulTest Class.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public abstract class ExternalEventModulTest{

	TestModel model;
	TestExternalEvent externalEvent;
	@BeforeEach
	public void setUp() throws Exception {
		
		this.model = new TestModel();
		Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);
		model.connectToExperiment(experiment);
		this.externalEvent = new TestExternalEvent(model, "First Test ExternalEvent", false);
	}

	/**
	 * This checks if the event is external or not.
	 */
	public void testIsExternal()
	{
		assertTrue(externalEvent.isExternal());
	}
	/**
	 * This tests checks the given name.
	 */
	public void testName()
	{
		assertEquals("First Test ExternalEvent#1", externalEvent.getName());
	}
	
	
	@AfterEach
	public void tearDown() throws Exception {
	}

}
