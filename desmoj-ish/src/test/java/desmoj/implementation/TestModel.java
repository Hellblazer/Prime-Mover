package desmoj.implementation;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.concurrent.TimeUnit;

/**
 * A simple testmodel to provide a reference for the modultest implementations
 *
 * @param owner       model
 * @param name        java.lang.String
 * @param showInTrace
 * @author Sascha Winde
 */
public class TestModel extends Model {

    // define model components here
    TestSimProcess process;

    public TestModel() {
        super(null, "<Test Model>", true, true);
    }

    /** runs the model */
    public static void main(String[] args) {

        // create model and experiment
        TestModel model = new TestModel();
        Experiment exp = new Experiment("Test Experiment");
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(true);
        TimeInstant stopTime = new TimeInstant(1440, TimeUnit.MINUTES);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);

        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }

    /** returns a description of this model to be used in the report */
    @Override
    public String description() {
        return "<Test description>";
    }

    /**
     * activate dynamic components
     */
    @Override
    public void doInitialSchedules() {
        process.activatePreempt();
    }

    // define any additional methods if necessary,
    // e.g. access methods to model components

    /** initialise static components */
    @Override
    public void init() {
        this.process = new TestSimProcess(this, "First Test SimProcess", false);
    }

}
