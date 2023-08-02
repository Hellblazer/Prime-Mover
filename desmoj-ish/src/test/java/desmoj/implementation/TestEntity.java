package desmoj.implementation;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.EventNote;
import desmoj.core.simulator.Model;

/**
 * This TestEntity is generated to have the ability to test
 * all Classes, which need an entity to operate on.
 * 
 * @author Clara Bluemm
 * 
 * @see eventList
 * @see EventNote
 * @see Event
 * @see Scheduler
 */
public class TestEntity extends Entity {
	
	/**
	 * For testing.
	 */
	public int count;

	/**
	 * 
	 * @param owner : the model this entity belongs to
	 * @param name : the name of the entity
	 * @param showInTrace : indicates whether it is shown in a trace
	 */
	public TestEntity(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}
	
	/**
	 * To have the option to change the Entities inner state
	 * this method changes the variable count.
	 * 
	 * @param int : the integer to be added 
	 */
	public void add(int i){
		count = count + i;
	}
	
	/**
	 * To validate the inner change of the entity, this
	 * method can be used
	 * 
	 * @return int : the value of the testVariable count
	 */
	public int getCount(){
		return count;
	}
}
