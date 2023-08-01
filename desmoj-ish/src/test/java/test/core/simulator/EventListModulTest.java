package test.core.simulator;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.EventList;
import desmoj.core.simulator.EventNote;
import desmoj.core.simulator.EventTreeList;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Scheduler;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import test.implementation.TestEntity;
import test.implementation.TestEvent;
import test.implementation.TestModel;

/**
 * Since all Methods are declared abstract in test.core.simulator.EventList
 * it shouldn't be much in here. But because both implementations will 
 * have the same behavior, I put the code in here and not into the
 * derived classes EventTreeListModulTest and EventVectorListModulTest.
 * <p>
 * Since all the methods are declared as default it is not possible to
 * test them.
 * 
 * @author Clara Bluemm, Sascha Winde
 *
 * @see eventList
 * @see EventTreeList
 * @see EventVectorList
 */
public abstract class EventListModulTest
{
	private Experiment experiment;
	private TestModel model;
	private SimClock clock;
	private Scheduler scheduler;
	private EventList list;
	private EventNote note1;
	private EventNote note2;
	private EventNote note3;
	private TestEntity enty1;
	private TestEntity enty2;
	private TestEntity enty3;
	private TestEvent event1;
	private TestEvent event2;
	private TestEvent event3;

	public abstract EventList getList();

	/**
	 * Instantiates the needed Objects to run the tests against. 
	 * Is made after every test in this class.
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
		this.clock = experiment.getSimClock();
		this.scheduler = experiment.getScheduler();
		this.list = getList();
		this.enty1 = new TestEntity(model, "enty1", false);
		this.enty2 = new TestEntity(model, "enty2", false);
		this.enty3 = new TestEntity(model, "enty3", false);
		this.event1 = new TestEvent(model, "event1", false);
		this.event2 = new TestEvent(model, "event2", false);
		this.event3 = new TestEvent(model, "event3", false);
		this.note1 = new EventNote(enty1, event1, new TimeInstant(1));
		this.note2 = new EventNote(enty2, event2, new TimeInstant(2));
		this.note3 = new EventNote(enty3, event3, new TimeInstant(3));
	}
	
	/**
	 * Makes the User create new event-notes.
	 *@see EventNoteTest
	 */
	
	public void createEventNote(){
		// look at EventNoteTest to see the different abilities
		// of EventNote
	}
	
	/**
	 * Returns the first event-note which is stored in the 
	 * EventList.
	 */
	
	public void firstNote(){
		//TODO declared as default
	}
	
	public void getEventNote(){
		//TODO declared as default
	}
	
	public void insert(){
		//TODO declared as default
	}
	
	public void insertAfter(){
		//TODO declared as default
	}
	
	public void insertAsFirst(){
		//TODO declared as default
	}
	
	public void insertAsLast(){
		//TODO declared as default
	}
	
	public void insertBefore(){
		//TODO declared as default
	}
	
	public void isEmpty(){
		//TODO declared as default
	}
	
	public void lastNote(){
		//TODO declared as default
	}
	
	public void nextNote(){
		//TODO declared as default
	}
	
	public void prevNote(){
		//TODO declared as default
	}
	
	public void remove(){
		//TODO declared as default
	}
	
	public void removeFirst(){
		//TODO declared as default
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

}
