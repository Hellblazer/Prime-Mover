package test.implementation;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * This class is an subclass to an abstact simulation core class.
 * This class is implemented to be able to test the functionality
 * provided by the Mainclass.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class TestExternalEvent extends ExternalEvent
{

	public TestExternalEvent(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine() {
		// TODO Auto-generated method stub
		
	}

}
