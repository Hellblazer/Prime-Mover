/**
 * 
 */
package test.implementation;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

/**
 * This class is implemented for testing the class EventNote.
 * It implements the super class EventTwoEntities
 * 
 * @author Clara Bluemm, Sascha Winde
 * @see TestFramework.core.simulator.EventNoteModulTest
 * @see test.core.simulator.EventTwoEntities
 */
public class TestEventTwoEntities extends EventOf2Entities {

	/**
	 * @param owner : the model this event belongs to
	 * @param name : the name of the event
	 * @param showInTrace : indicates whether this event is shown in a trace
	 */
	public TestEventTwoEntities(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}

	/** 
	 * The routine which changes the inner state of every
	 * given entity. Here it adds 2 two the variable count 
	 * of the entity.
	 * @see desmoj.core.simulator.EventTwoEntities#eventRoutine(desmoj.core.simulator.Entity, desmoj.core.simulator.Entity)
	 */
	@Override
	public void eventRoutine(Entity who1, Entity who2) {
		((TestEntity) who1).add(2);
		((TestEntity) who2).add(2);
	}

}
