/**
 * 
 */
package test.implementation;


import desmoj.core.simulator.Entity;
import desmoj.core.simulator.EventOf3Entities;
import desmoj.core.simulator.Model;

/**
 * This class is implemented for testing the class EventNote.
 * It implements the super class EventThreeEntities
 * 
 * @author Clara Bluemm
 * @see TestFramework.core.simulator.EventNoteModulTest
 * @see test.core.simulator.EventTwoEntities
 *
 */
public class TestEventThreeEntities extends EventOf3Entities {

	/**
	 * @param owner
	 * @param name
	 * @param showInTrace
	 */
	public TestEventThreeEntities(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}

	/** 
	 * The routine which changes the inner state of all three 
	 * of the given Entities. It adds 3 to the variable count
	 * of all TestEntities.
	 * @see desmoj.core.simulator.EventThreeEntities#eventRoutine(desmoj.core.simulator.Entity, desmoj.core.simulator.Entity, desmoj.core.simulator.Entity)
	 */
	@Override
	public void eventRoutine(Entity who1, Entity who2, Entity who3) {
		((TestEntity) who1).add(3);
		((TestEntity) who2).add(3);
		((TestEntity) who3).add(3);
	}

}
