package desmoj.implementation;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 *  A simple testmodel to provide a reference for the modultest implementations
 *
 * @author Sascha Winde
 *
 * @param owner model
 * @param name java.lang.String
 * @param showInTrace
 */
public class TestSubModel extends Model {



	   public TestSubModel(Model owner) {
	      super(owner, "<Test SubModel>", true, true);
	   }

	   /** initialise static components */
	   @Override
    public void init() {
	   }

	   /** activate dynamic components */
	   @Override
    public void doInitialSchedules() {
	   }

	   /** returns a description of this model to be used in the report */
	   @Override
    public String description() {
	      return "<Test description>";
	   }

	   // define any additional methods if necessary,
	   // e.g. access methods to model components

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

	}
