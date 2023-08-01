package test.implementation;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * This class is derived from the abstract class Event.
 * It has to override only one method, which describes the 
 * Routine, which changes the inner state of the entity.
 * 
 * @author Clara Bluemm
 * 
 * @see desmoj.core.simulator.Event
 * @see desmoj.core.simulator.EventAbstract
 */
public class TestEvent extends Event {

	/**
	 * @param owner : the model, this event is part of
	 * @param name : name of the event
	 * @param showInTrace : indicates whether this event is shown in the trace
	 */
	public TestEvent(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}

	/** 
	 * The Routine to change the inner state of the given Entity.
	 * It adds 1 to the variable count.
	 * 
	 * @param entity : the entity to be changed
	 * @see desmoj.core.simulator.Event#eventRoutine(desmoj.core.simulator.Entity)
	 */
	@Override
	public void eventRoutine(Entity who) {
		((TestEntity) who).add(1);
	}

}
