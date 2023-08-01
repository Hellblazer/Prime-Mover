package test.core.simulator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.core.simulator.EventNote;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import test.implementation.TestEntity;
import test.implementation.TestEvent;
import test.implementation.TestEventAbstract;
import test.implementation.TestEventThreeEntities;
import test.implementation.TestEventTwoEntities;
import test.implementation.TestModel;

/**
 * This class tests all visible methods of the the event-note class.
 * It uses the classes named below to create a test fixure. This classes
 * are found in the test package.testImplementation.
 * 
 * @author Clara Bluemm
 * 
 * @see TestEntity
 * @see TestEvent
 * @see TestEventTwoEntities
 * @see TestEventThreeEntities
 */
public class EventNoteModulTest{

	private Experiment experiment;
	private TestModel model;
	private EventNote note1;
	private EventNote note2;
	private EventNote note3;
	private TestEntity enty1;
	private TestEntity enty2;
	private TestEntity enty3;
	private TestEventAbstract event;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		//experiment has to be created to avoid NullpointExcepions
		this.experiment = new Experiment("Test Experiment", 
				java.util.concurrent.TimeUnit.SECONDS, 
				java.util.concurrent.TimeUnit.HOURS, null);
		//a model has to be created to avoid NullpointerExceptions
		this.model = new TestModel();
		//and both have to be connected to avoid NullPointerException
		model.connectToExperiment(experiment);
		this.enty1 = new TestEntity(model, "enty1", false);
		this.enty2 = new TestEntity(model, "enty2", false);
		this.enty3 = new TestEntity(model, "enty3", false);
		this.event = new TestEventAbstract(model, "event1", false);
		this.note1 = new EventNote(enty1, event, new TimeInstant(1));
		this.note2 = new EventNote(enty1, enty2, event, new TimeInstant(2));
		this.note3 = new EventNote(enty1, enty2, enty3, event, new TimeInstant(3));
	}
	
	/**
	 * Tests the method, which shall return the clone of 
	 * this event-note.
	 */
	@Test
	public void testClone(){
		assertNotSame(note1, note1.clone());
		assertEquals(note1.getEntity1(), note1.clone().getEntity1());
		assertEquals(note2.getEntity1(), note2.clone().getEntity1());
		assertEquals(note2.getEntity2(), note2.clone().getEntity2());
		assertEquals(note3.getEntity1(), note3.clone().getEntity1());
		assertEquals(note3.getEntity2(), note3.clone().getEntity2());
		assertEquals(note3.getEntity3(), note3.clone().getEntity3());
	}
	
	/**
	 * Looks whether the event-notes equals another one.
	 */
	@Test
	public void testEquals(){
		TestEvent event4 = new TestEvent(model, "event4", false);
		EventNote note4 = new EventNote(enty1, event4, new TimeInstant(1));
		EventNote note5 = new EventNote(enty1, event,  new TimeInstant(1));
		// note5 holds the same information as note1
		assertTrue(note1.equals(note5));
		assertFalse(note1.equals(note4));
		assertTrue(note1.equals(note1));
		assertFalse(note1.equals(note4));
		assertFalse(note1.equals(note2));
		//throws Nullpointer
		assertFalse(note3.equals(null));
	}
	
	/**
	 * Compares the two Event notes, whether the given EventNote
	 * takes place before, on or after the actual EventNote.
	 */
	@Test
	public void testCompareTo(){
		assertEquals(note1.compareTo(note1), 0);
		assertEquals(note1.compareTo(note2), -1);
		assertEquals(note2.compareTo(note1), +1);
		assertEquals(note3.compareTo(note2), +1);
	}
	
	/**
	 * Tests, whether the right Entities are returned.
	 * This Tests all three methods: getEntity1(), getEntity2, getEntity3
	 */
	@Test
	public void testGetEntity(){
		assertTrue(note1.getEntity1().equals(enty1));
		assertFalse(note1.getEntity1().equals(enty2));
		assertTrue(note3.getEntity3().equals(enty3));
		//throws Nullpointer, since note1 only has one entity
//		assertFalse(note1.getEntity2().equals(enty2));
	}
	
	/**
	 * Tests, whether the right Event is given back.
	 * This tests all three methods: getEvent1, getEvent2, getEvent3
	 */
	@Test
	public void testGetEvent()
	{
		assertSame(note1.getEvent().toString(), event.toString());
		assertSame(note1.getEvent().toString(), note2.getEvent().toString());
		assertSame(note3.getEvent().toString(), event.toString());
		//throws Nullpointer, since note1 only has one entity
//		assertNotSame(note1.getEvent2().toString(), event3);
	}
	
	/**
	 * Evaluates whether the right number of referenced Entities is
	 * given back.
	 */
	@Test
	public void testGetNumberOfEntities(){
		assertEquals(note1.getNumberOfEntities(), 1);
		assertEquals(note2.getNumberOfEntities(), 2);
		assertEquals(note3.getNumberOfEntities(), 3);
	}
	
	/**
	 * Tests whether the right time is given back.
	 */
	@Test
	public void testTime()
	{
		assertEquals(new TimeInstant(1), note1.getTime());
	}
	
	/**
	 * All method to set Entity are declared as default.
	 */
	
	public void setEntities(){
		//declared default, not visible, no testing here
	}
	
	/**
	 * All methods to set events are declared as default.
	 */
	
	public void setEvents(){
		//declared default, not visible, no testing here
	}
	
	/**
	 * All methods to set the time are declared as default.
	 */
	
	public void setTimes(){
		//declared default, not visible, no testing here
	}
	
	
	/**
	 * The set and get method to set the flag whether the event-note
	 * is connected to its predecessor.
	 */
	
	public void setConnected(){
		//declared default, not visible, no testing here
	}
	
	/**
	 * Tests the method, which makes the representation of 
	 * the event-note as a string.
	 */
	@Test
	public void testToString(){
		String s = "En1:enty1#1 En2:enty2#1 En3:enty3#1 Ev:event1#1 t:3.0000";
		assertEquals(note3.toString(), s);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	public void tearDown() throws Exception {
	}

}
