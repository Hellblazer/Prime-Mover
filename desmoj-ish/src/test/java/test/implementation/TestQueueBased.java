/**
 * 
 */
package test.implementation;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.TimeInstant;

/**
 * TestImplementation for the Class QueueBased. 
 * To be able to initiate a Queue for testing.
 * @author Clara Bluemm
 * @see desmoj.core.simulator.QueueBased
 *
 */
public class TestQueueBased extends QueueBased
{

	private TestReporter reporter;


	/**
	 * Constructor derived from Class QueueBased.
	 * 
	 * @param owner
	 *            desmoj.Model : The model it belongs to
	 * @param name
	 *            java.lang.String : The name for this QueueBased object
	 * @param showInReport
	 *            boolean : Flag if values are shown in report
	 * @param showInTrace
	 *            boolean : Flag if QueueBased writes trace messages
	 */
	public TestQueueBased(Model owner, String name, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		// Auto-generated constructor stub
	}

	/**
	 * Creates the Reporter of this Class.
	 */
	@Override
	public Reporter createDefaultReporter() {
		this.reporter = new TestReporter();
		return reporter;
	}
	
	/**
	 * Method to insert an Item into the Queue.
	 * Since these operation is specified in the underlying
	 * classes and the method updateStatistics() is declared "protected", 
	 * this method should provide an opportunity
	 * to test the statistics.
	 * @param i	
	 * 			int: amount of elements to be inserted
	 */
	public	void insert(int i)
	{
		for(int k=1; k<=i; k++)
			{
				super.addItem();
			};
	}
	
	/**
	 * Also the method "deleteItem" is declared as "protected".
	 * For testing reasons this method is created.
	 *
	 * 	 * @param entryTime
	 *            TimeInstant : Point of simulation time that the object now
	 *            exiting the queuebased had entered it
	 */
	public void delete(TimeInstant entryTime)
	{
		
	}
}
