package test.implementation;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.ArrivalProcess;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
/**
 * This class is an subclass to an abstact simulation core class.
 * This class is implemented to be able to test the functionality
 * provided by the Mainclass.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class TestArrivalProcess extends ArrivalProcess {

	TestSimProcess simProcess;
	
	
	public TestArrivalProcess(Model owner, String name, NumericalDist arrivalRate,
			boolean showInTrace) {
		super(owner, name, arrivalRate, showInTrace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SimProcess createSuccessor() {
		this.simProcess = new TestSimProcess(currentModel(), "Successor process", false);
		return simProcess;
	}

}
