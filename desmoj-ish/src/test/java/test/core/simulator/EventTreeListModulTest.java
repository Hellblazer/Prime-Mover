/**
 * 
 */
package test.core.simulator;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.EventList;
import desmoj.core.simulator.EventTreeList;

/**
 * This class represents the implementation of the event-list as 
 * a treeList. It only returns an object of this implementation
 * to its super class. All testing methods are implemented in the
 * super class EventListModulTest.
 * 
 * @author Clara Bluemm
 * @see EventListModulTest
 */
public class EventTreeListModulTest extends EventListModulTest{

	private EventTreeList list;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		this.list = new EventTreeList();
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	/**
	 * Returns the implementation of the event-list as a EventTreeList.
	 * This is used for testing of the super class EventListModulTest.
	 */
	@Override
	public EventList getList() {
		return this.list;
	}

}
