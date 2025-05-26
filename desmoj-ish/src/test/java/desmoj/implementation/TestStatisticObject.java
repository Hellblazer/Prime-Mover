package desmoj.implementation;

import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

/**
 * This class is an subclass to an abstact simulation core class. This class is implemented to be able to test the
 * functionality provided by the Mainclass.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public class TestStatisticObject extends StatisticObject {

    public TestStatisticObject(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);
    }

    @Override
    public void update(Observable arg0, Object arg1) {

    }

}
