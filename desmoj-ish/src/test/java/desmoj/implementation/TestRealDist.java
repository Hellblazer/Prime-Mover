package desmoj.implementation;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

/**
 * This class is an subclass to an abstact simulation core class. This class is
 * implemented to be able to test the functionality provided by the Mainclass.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class TestRealDist extends NumericalDist<Double> {

    public TestRealDist(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Double getInverseOfCumulativeProbabilityFunction(double p) {
        // TODO Auto-generated method stub
        return 10d;
    }

    @Override
    public Double sample() {
        // TODO Auto-generated method stub
        return 10d;
    }

}
