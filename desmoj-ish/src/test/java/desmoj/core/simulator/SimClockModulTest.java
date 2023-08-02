package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;

/**
 * This Class tests the SimClock on it's own.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class SimClockModulTest{

	private SimClock testClock;
	
	public void setUp(){
	       Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);

	       
		this.testClock = new SimClock("Test Clock");
		
	}
	/**
	 * This Test checks the Simclocks given name.
	 */
	public void testName()
	{
		String name = testClock.getName();
		assertEquals("Test Clock_clock", name);
	}
	
	/**
	 * This Test checks the SimClocks given Time as double.
	 */
	public void testTime()
	{
		TimeInstant i = new TimeInstant(0);
		assertTrue(i.getTimeAsDouble() == testClock.getTime().getTimeAsDouble());
	}
	
	public void tearDown()
	{
		
	}
}
