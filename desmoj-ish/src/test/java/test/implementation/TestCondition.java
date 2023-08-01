package test.implementation;

import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * This class is an subclass to an abstact simulation core class.
 * This class is implemented to be able to test the functionality
 * provided by the Mainclass.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class TestCondition extends Condition{
	
	int a = 10;
	
	public TestCondition(Model owner, String name, boolean showInTrace,
			Object[] args) {
		super(owner, name, showInTrace, args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check() {
		
		return true;
	}

	@Override
	public boolean check(Entity e) {
		// TODO Auto-generated method stub
		return false;
	}

}
