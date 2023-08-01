/**
 * 
 */
package test.implementation;

import java.beans.PropertyChangeEvent;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.QueueListStandard;

/**
 * This class is used to be able to initiate an object
 * of typ QueueList for testing reasons.
 * 
 * @author Clara Bluemm
 *
 */
public class TestQueueList extends QueueListStandard
{

	public TestQueueList() {
	}

	public void propertyChange(PropertyChangeEvent arg0) {
	}

	@Override
	public Entity first() {
		return null;
	}

	@Override
	public Entity get(int index) {
		return null;
	}

	@Override
	public int get(Entity element) {
		return 0;
	}

	@Override
	public String getAbbreviation() {
		return null;
	}

	@Override
	public void insert(Entity e) {
		
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean remove(Entity e) {
		return false;
	}

	@Override
	public boolean remove(int index) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Entity succ(Entity e) {
		return null;
	}

	@Override
	public String toString() {
		return null;
	}
}
